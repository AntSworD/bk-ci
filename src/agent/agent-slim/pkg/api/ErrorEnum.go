package api

type ErrorCode int

const (
	BuildProcessRunError ErrorCode = 2128040 + iota
	RecoverRunFileError
	LoseRunFileError
	MakeTmpDirError
	BuildProcessStartError
	PrepareScriptCreateError
	DockerOsError
	DockerRunShInitError
	DockerRunShStatError
	DockerClientCreateError
	DockerImagesFetchError
	DockerImagePullError
	DockerMakeTmpDirError
	DockerMountCreateError
	DockerContainerCreateError
	DockerContainerStartError
	DockerContainerRunError
	DockerContainerDoneStatusError
	DockerChmodInitshError
	DockerCredGetError
	DockerDockerOptions
	DockerImageDebugError
)

type ErrorTypes string

const (
	System     ErrorTypes = "SYSTEM"
	User       ErrorTypes = "USER"
	ThirdParty ErrorTypes = "THIRD_PARTY"
	Plugin     ErrorTypes = "PLUGIN"
)

type ErrorEnum struct {
	Type    ErrorTypes
	Code    ErrorCode
	Message string
}

var (
	NoErrorEnum                  = &ErrorEnum{Type: "", Code: 0, Message: ""}
	BuildProcessRunErrorEnum     = &ErrorEnum{Type: User, Code: BuildProcessRunError, Message: "构建进程执行错误"}
	RecoverRunFileErrorEnum      = &ErrorEnum{Type: User, Code: RecoverRunFileError, Message: "恢复执行文件失败错误"}
	LoseRunFileErrorEnum         = &ErrorEnum{Type: User, Code: LoseRunFileError, Message: "丢失执行文件失败错误"}
	MakeTmpDirErrorEnum          = &ErrorEnum{Type: User, Code: MakeTmpDirError, Message: "创建临时目录失败"}
	BuildProcessStartErrorEnum   = &ErrorEnum{Type: User, Code: BuildProcessStartError, Message: "启动构建进程失败"}
	PrepareScriptCreateErrorEnum = &ErrorEnum{Type: User, Code: PrepareScriptCreateError, Message: "预构建脚本创建失败"}
)

func (t *PersistenceBuildInfo) ToFinish(
	success bool,
	message string,
	errorEnum *ErrorEnum,
) *PersistenceBuildWithStatus {
	if success || errorEnum == NoErrorEnum {
		return &PersistenceBuildWithStatus{
			PersistenceBuildInfo: *t,
			Success:             success,
			Message:             message,
			Error:               nil,
		}
	}
	return &PersistenceBuildWithStatus{
		PersistenceBuildInfo: *t,
		Success:             success,
		Message:             message,
		Error: &Error{
			ErrorType:    errorEnum.Type,
			ErrorMessage: errorEnum.Message,
			ErrorCode:    errorEnum.Code,
		},
	}
}
