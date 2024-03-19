import json
import os

import pathvalidate
import shapely
import sys
from pathlib import Path
from uuid import UUID


def check_guid(value: str) -> bool:

    try:
        uuid_obj = UUID(value, version=4)

    except ValueError:
        return False

    return str(uuid_obj) == value


def check_positive_integer(value: str) -> bool:

    try:
        int_value: int = int(value)

    except ValueError:
        return False

    return int_value > 0


def check_json(value: str) -> bool:

    try:
        json.loads(value)

    except ValueError:
        return False

    return True


def check_parameter(parameter_name: str, parameter_value: str) -> (bool, str):
    is_valid: bool
    error_message: str = ""

    match parameter_name:

        case "Client":
            is_valid = check_guid(parameter_value)

            if not is_valid:
                error_message = "Value for parameter {} is not a valid GUID: {}".format(parameter_name, parameter_value)

        case "FolderOut":
            is_valid = check_path(parameter_value)

            if not is_valid:
                error_message = "Value for parameter {} is not a valid path: {}".format(parameter_name, parameter_value)

        case "OrderLabel":
            is_valid = parameter_value is not None and parameter_value != ""

            if not is_valid:
                error_message = "Value for parameter {} is null or empty".format(parameter_name)

        case "Organism":
            is_valid = check_guid(parameter_value)

            if not is_valid:
                error_message = "Value for parameter {} is not a valid GUID: {}".format(parameter_name, parameter_value)

        case "Parameters":
            is_valid = check_json(parameter_value)
            
            if not  is_valid:
                error_message = "Value for parameter {} is not valid JSON: {}".format(parameter_name, parameter_value)

        case "Perimeter":
            is_valid = check_wkt(parameter_value)

            if not is_valid:
                error_message = "Value is not a valid WKT polygon: {}".format(parameter_value)

        case "Product":
            is_valid = check_guid(parameter_value)

            if not is_valid:
                error_message = "Value for parameter {} is not a valid GUID: {}".format(parameter_name, parameter_value)

        case "Request":
            is_valid = check_positive_integer(parameter_value)

            if not is_valid:
                error_message = "Value for parameter {} is not a valid positive integer: {}".format(parameter_name,
                                                                                                    parameter_value)

        case _:
            is_valid = True

    return is_valid, error_message


def check_path(value: str) -> bool:
    return pathvalidate.is_valid_filepath(value, platform="auto")


def check_wkt(value: str) -> bool:
    geometry: shapely.Geometry = shapely.from_wkt(value, "ignore")

    return geometry is not None and shapely.get_type_id(geometry) == shapely.GeometryType.POLYGON


if __name__ == "__main__":
    output_folder_path: str
    print("Reading {} arguments: {}".format(len(sys.argv), sys.argv))
    arguments: list[str] = sys.argv
    parameters_list: list[str] = ["Client", "FolderOut", "OrderLabel", "Organism", "Parameters", "Perimeter", "Product",
                                  "Request"]

    if len(arguments) < 2:
        sys.stderr.write("No FME script provided\n")
        sys.exit(10)

    fme_script_name: str = arguments[1]

    if not check_path(fme_script_name):
        sys.stderr.write("The FME script path is invalid: {}\n".format(fme_script_name))
        sys.exit(15)

    if len(arguments) < 3:
        sys.stderr.write("No script parameter provided\n")
        sys.exit(20)

    remaining_arguments: list[str] = parameters_list.copy()

    for argument_index in range(2, len(arguments), 2):
        parameter_name_raw: str = arguments[argument_index]

        if not parameter_name_raw.startswith("--"):
            sys.stderr.write("Positional parameter in script parameter strings: {}\n".format(parameter_name_raw))
            sys.exit(30)

        parameter_name: str = parameter_name_raw[2:]
        parameter_value: str = arguments[argument_index + 1]

        print("Reading parameter {} with value {}".format(parameter_name, parameter_value))

        if parameter_name in remaining_arguments:
            remaining_arguments.remove(parameter_name)

            is_parameter_valid, message = check_parameter(parameter_name, parameter_value)

            if not is_parameter_valid:
                sys.stderr.write("{}\n".format(message))
                sys.exit(40)

            else:

                if parameter_name == "FolderOut":
                    output_folder_path = parameter_value

        else:

            if parameter_name not in parameters_list:
                # Unknown parameter is not an error as it stands now
                continue

            else:
                sys.stderr.write("Parameter {} defined more than once\n")
                sys.exit(30)

    if len(remaining_arguments) > 0:
        sys.stderr.write("The following parameters are missing: {}\n".format(remaining_arguments))
        sys.exit(50)

    # Simulates an FME error
    if Path(fme_script_name).stem.endswith("_fails"):
        sys.stderr.write("The FME scripts resulted in an error (simulated)\n")
        sys.exit(100)

    # Writes a dummy result file in the output folder, unless instructed not to do so (to test script success
    # with no result)
    if not Path(fme_script_name).stem.endswith("_nofiles"):
        output_file = open(os.path.join(output_folder_path, "dummy.txt"))
        output_file.write("Dummy result file content.");
        output_file.close()

    sys.exit(0)
