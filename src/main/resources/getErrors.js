function getErrors(input) {
    if (!JSHINT("alert('foo');", JSLINT_OPTIONS)) {
       // print(input)
        for (var errorIndex = 0; errorIndex < JSHINT.errors.length; errorIndex++) {
            var error = JSHINT.errors[errorIndex];
            if (error != null) {
                print('    ' + error.reason + ' (line: ' + error.line + ', character: ' + error.character + ')');
                print('    > ' + (error.evidence || '').replace(/^\s*(\S*(\s+\S+)*)\s*$/, "$1") + '\n');
            }
        }
        return JSHINT.errors.length;
    }

    return 0;
};