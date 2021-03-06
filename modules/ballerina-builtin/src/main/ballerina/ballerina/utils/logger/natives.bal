package ballerina.utils.logger;

import ballerina.doc;

@doc:Description {value: "Logs the specified value at debug level."}
@doc:Param {value: "value: The value to be logged."}
public native function debug(any value);

@doc:Description {value: "Logs the specified value at error level."}
@doc:Param {value: "value: The value to be logged."}
public native function error(any value);

@doc:Description {value: "Logs the specified value at info level."}
@doc:Param {value: "value: The value to be logged."}
public native function info(any value);

@doc:Description {value: "Logs the specified value at trace level."}
@doc:Param {value: "value: The value to be logged."}
public native function trace(any value);

@doc:Description {value: "Logs the specified value at warn level."}
@doc:Param {value: "value: The value to be logged."}
public native function warn(any value);