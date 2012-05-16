function getErrors(input, filename) {
    if (!JSHINT(input, JSLINT_OPTIONS)) {
        print('errors found in ' + filename + ":")
        for (var errorIndex = 0; errorIndex < JSHINT.errors.length; errorIndex++) {

            var error = JSHINT.errors[errorIndex];
            if (error != null) {
                print('    ' + error.reason + ' (line: ' + error.line + ', character: ' + error.character + ')');
                print('    > ' + (error.evidence || '').replace(/^\s*(\S*(\s+\S+)*)\s*$/, "$1") + '\n');
            }
        }
        return JSHINT.errors.length;
    } else {
        print('no errors found in ' + filename + '\n')
    }
    return 0;
};