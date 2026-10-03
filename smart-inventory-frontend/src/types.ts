export type AuthCredentials = {
  username: string
  password: string
}

export type Vehicle = {
  id: number
  vin: string
  vinShort: string
  typeName: string | null
  brand: string | null
  vehicleCondition: string | null
  registrationNumber: string | null
  acquisitionDate: string | null
  status: string
  vatType: string | null
  totalCostAmount: number
  advertisedPriceGross: number | null
  priceSource: string | null
  priceUpdatedAt: string | null
  lastSeenAt: string | null
}

export type VehicleImportRowError = {
  sourceRowNumber: number
  message: string
}

export type VehicleImportResult = {
  originalFilename: string
  totalRows: number
  insertedRows: number
  skippedRows: number
  rejectedRows: number
  errors: VehicleImportRowError[]
}

export type VehicleImportHistoryItem = VehicleImportResult & {
  id: number
  importedAt: string
}
