# ISSUE_391 - FME Form V2: pass FolderIn and FolderOut on the command line

## Status: COMPLIANT

### Issue Description

The `Extraction FME Form (Version 2)` plugin hands every request parameter to the workspace through the
`parameters.json` file. Some parameters are needed by FME at a higher level, before the extraction starts and
before the file is read; those must reach the workspace as command-line arguments, the way the classic plugin
passes all of them. The issue asks for `FolderIn` and `FolderOut` to be passed on the command line as well, while
`parameters.json` stays as it is.

### Implementation Completed

| File | Change |
| --- | --- |
| `FmeDesktopV2Plugin.java` | The command line is now assembled by a package-private `buildCommand()`: executable, workspace, `--parametersFile <file>`, then `--FolderIn <folder>` and `--FolderOut <folder>`. `launchFmeTaskProcess()` calls it; nothing else in the launch changes. The argument names come from the configuration, through a `formatParameterName()` like the classic plugin's. |
| `config.properties` | New key `paramRequestFolderIn=FolderIn`, so the argument name is defined next to the existing `paramRequestFolderOut=FolderOut`. |
| `help.html` (fr, de, en) | New "Command-line arguments" section with the example command. The introduction also said `parameters.json` was created in the output directory: it is created in the input directory (`FmeDesktopV2Plugin.execute()`), and now says so. |

### Decision: `parameters.json` is unchanged

Criterion 391-1 asks for both parameters in the command line **and** in `parameters.json`; criterion 391-3 asks
for `parameters.json` to stay unchanged. `FolderOut` satisfies both, since the file already carries it. `FolderIn`
has never been in the file, so the two criteria cannot both hold for it. The file stays unchanged, as 391-3
requires: `FolderIn` is passed on the command line only.

The arguments are passed through `ProcessBuilder` as a list, not through a shell, so paths holding spaces need no
quoting and get none.

### Tests

`FmeDesktopV2PluginTest.testBuildCommandPassesFoldersAsArguments` (unit) asserts the exact command list, order
included: a wrong argument name, a missing value or an inverted pair fails it.

Run end to end against a fake `fme` script that dumps its arguments: the script received
`workspace.fmw --parametersFile …/in/parameters.json --FolderIn …/in --FolderOut …/out`, and the
`parameters.json` written next to it carried `FolderOut` and no `FolderIn`, as before.

### Documentation / i18n impact

- Plugin help in French, German and English: new section, corrected directory.
- `docs/how-to/fme-form.md`: the step that declares the `parametersFile` user parameter now shows the two extra
  arguments and how to declare them.
- Database: no migration. `parameters.json`: unchanged.

### Conclusion

`FolderIn` and `FolderOut` reach the workspace as command-line arguments, usable as user parameters before the
parameters file is read; the file itself is untouched.
