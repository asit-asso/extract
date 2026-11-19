# ISSUE_350 - Remove the paramInputData=SourceDataset_FILEGDB parameter

## Status: ✅ COMPLIANT

### Issue Description
The `paramInputData=SourceDataset_FILEGDB` parameter in
`extract-task-fmedesktop/src/main/resources/plugins/fme/properties/configFME.properties`
was unused and had to be removed.

### Conformity Analysis
**COMPLIANT** - Verification performed: the `paramInputData` property is not consumed by
any application code. It was only referenced by the `.properties` file itself and by a
unit test asserting its value.

### Implementation Completed
1. Removed the `paramInputData=SourceDataset_FILEGDB` line from `configFME.properties`.
2. Removed the now-obsolete test `returnsParamInputDataProperty`
   (`PluginConfigurationTest`).

### Tests
No new tests: this is the removal of unused configuration. The test covering the removed
property was deleted; the rest of `PluginConfigurationTest` keeps covering the properties
that are actually used.

### Documentation / i18n impact
None: the property does not appear in the architecture documentation and does not
correspond to any multilingual label.

### Conclusion
The unused parameter is removed with no impact on the application's behaviour.
