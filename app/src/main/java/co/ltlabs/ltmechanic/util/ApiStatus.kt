package co.ltlabs.ltmechanic.util

enum class ApiStatus { LOADING, ERROR, DONE }

enum class ApiCallStatus { SUCESS, TOKEN_EXPIRED, UNAUTHORIZED }

enum class MachineStatus { FOUND, NOT_FOUND }

enum class MachineCheckinStatus { SUCCESS, FAILED, MACHINE_NOT_WORKING, MACHINE_CURRENTLY_IN_PLACE, NOT_IN_FLOATING_AREA, HAS_OPEN_TICKETS, USER_ON_FLOATING_AREA_ASSIGNED }

enum class MachineCheckoutStatus { SUCCESS, FAILED}

enum class LineAssignStatus { SUCCESS, FAILED }

enum class ClearLineStatus { SUCCESS, FAILED, WITH_TICKET, CLEARED }

enum class MachineInsertStatus { SUCCESS, FAILED, MACHINE_NOT_WORKING, MACHINE_CURRENTLY_IN_PLACE, NOT_IN_FLOATING_AREA, HAS_OPEN_TICKETS }

enum class CreateTicketStatus { SUCCESS, FAILED, ERROR, NO_ATTACHED_CHECKLIST, REACHED_REMARKS_LIMIT, HAS_OPEN_TICKETS }

enum class FileUploadStatus { SUCCESS, FAILED, ERROR }

enum class FindMachineStatus { SUCCESS, FAILED, NOT_FOUND }

enum class TicketUpdateStatus { SUCCESS, FAILED, HAS_OPEN_TICKETS }

enum class TicketReopenStatus { SUCCESS, FAILED, HAS_OPEN_TICKETS, TIME_EXCEEDED, NOT_ALLOWED }

enum class TicketStatus { IN_REPAIR, CANCELLED, REPAIRED, CLOSED, COMPLETED, IN_PROGRESS,  }

enum class ChecklistStatus { SUCCESS, FAILED, ERROR }

enum class LoginStatus { SUCCESS, FAILED, ERROR, USERNAME_INVALID, PASSWORD_INVALID }

enum class SetupStatus { SUCCESS, FAILED }

enum class LanguageStatus { SUCCESS, FAILED }

enum class AttachNFCStatus { SUCCESS, FAILED, DUPLICATE, ALREADY_ATTACHED }

enum class SendRequestStatus { SUCCESS, FAILED }

enum class ChangePasswordStatus { SUCCESS, FAILED, INCORRECT_PASSWORD, SAME_PASSWORD }

enum class ChangeFactoryStatus { SUCCESS, FAILED }