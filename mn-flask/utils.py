# The class defines a Result type, which can either be an error 
# or a correct result.

class Result:

    def __init__(self, ok, retValue=None, errMsg=None):
        self.ok = ok
        self.retValue = retValue
        self.errMsg = errMsg

    def isOk(self):
        return self.ok
    
    def isError(self):
        return not self.ok

    def Ok(retValue):
        return Result(True, retValue=retValue)

    def Error(errMsg):
        return Result(False, errMsg=errMsg)