export type OrderStatus =
  | 'PENDING_REVIEW'
  | 'PENDING_ASSIGN'
  | 'PENDING_ACCEPT'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'EVALUATED'
  | 'REJECTED'
  | 'CANCELLED'

export type OrderPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'
