# Set up an extraction using FME Form (desktop)

!!! info

    This tutorial uses the extraction plugin `FME Form (Version 2)` which allows unlimited parameters length, thus allowing request with precise geometries like municipalities boundary.

To follow along with this tutorial, download the sample script and data available [here](https://github.com/benoitregamey/extraction-samples). Either by cloning the repo :

```
git clone https://github.com/benoitregamey/extraction-samples.git
```

Or downloading a ZIP archive with the following link : https://github.com/benoitregamey/extraction-samples/archive/refs/heads/main.zip

First, let's explore what the workspace does.

1. In the downloaded folder, go to the subfolder `fme-form` and open up the `sample-script-FME2024.fmw` FME workspace.

2. The workspace uses a `creator` as a starting point.

3. The `FME Form (Version 2)` plugin sends all request parameters in a `GeoJSON` file. The latter is given in a CLI argument when running the workspace where the key is `--parametersFile` and the value is the path of the `GeoJSON` file. 

   To tell FME to use this argument, we need to create a User Parameter : 
   
   Under `User Parameters`, create a new `File/Folder/URL` parameter. The identifier must be `parametersFile` and you can deactivate the prompt option. For convenience, you can allocate a default value of `$(FME_MF_DIR)\parameters.json` to the parameter. Thus, when run inside FME, the default value is allocated to the `parametersFile` parameter and a local GeoJSON file is used. But when run by Extract, the `parametersFile` parameter will be overridden by the CLI argument and the GeoJSON from Extract will be used.