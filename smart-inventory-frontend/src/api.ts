import axios, { AxiosError } from 'axios'
import type {
  AuthCredentials,
  Vehicle,
  VehicleImportHistoryItem,
  VehicleImportResult,
} from './types'

const http = axios.create({
  baseURL: '',
  withCredentials: true,
})

function encodeBasicAuth(credentials: AuthCredentials) {
  const encoded = new TextEncoder().encode(
    `${credentials.username}:${credentials.password}`,
  )
  const binary = Array.from(encoded, (byte) => String.fromCharCode(byte)).join('')

  return `Basic ${btoa(binary)}`
}

function authHeaders(credentials: AuthCredentials) {
  return {
    Authorization: encodeBasicAuth(credentials),
  }
}

async function readCsrfToken() {
  const response = await http.get<string>('/login', {
    responseType: 'text',
    transformResponse: (value) => value,
  })

  const document = new DOMParser().parseFromString(response.data, 'text/html')
  const input = document.querySelector<HTMLInputElement>('input[name="_csrf"]')

  if (!input?.value) {
    throw new Error('Nem sikerult CSRF tokent kerni a szervertol.')
  }

  return input.value
}

export async function checkLogin(credentials: AuthCredentials) {
  await listVehicleImports(credentials)
}

export async function listVehicles(credentials: AuthCredentials) {
  const response = await http.get<Vehicle[]>('/api/v1/vehicles', {
    headers: authHeaders(credentials),
  })

  return response.data
}

export async function searchVehicles(
  credentials: AuthCredentials,
  vin: string,
) {
  const response = await http.get<Vehicle[]>('/api/v1/vehicles/search', {
    headers: authHeaders(credentials),
    params: { vin },
  })

  return response.data
}

export async function listVehicleImports(credentials: AuthCredentials) {
  const response = await http.get<VehicleImportHistoryItem[]>(
    '/api/v1/imports/vehicles/history',
    {
      headers: authHeaders(credentials),
    },
  )

  return response.data
}

export async function importVehicles(
  credentials: AuthCredentials,
  file: File,
) {
  const csrfToken = await readCsrfToken()
  const formData = new FormData()

  formData.append('_csrf', csrfToken)
  formData.append('file', file)

  const response = await http.post<VehicleImportResult>(
    '/api/v1/imports/vehicles',
    formData,
    {
      headers: {
        ...authHeaders(credentials),
        'X-CSRF-TOKEN': csrfToken,
      },
    },
  )

  return response.data
}

export function getApiErrorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    return getAxiosErrorMessage(error)
  }

  if (error instanceof Error) {
    return error.message
  }

  return 'Ismeretlen hiba tortent.'
}

function getAxiosErrorMessage(error: AxiosError) {
  const status = error.response?.status
  const data = error.response?.data

  if (typeof data === 'string' && data.trim()) {
    return data
  }

  if (data && typeof data === 'object') {
    const maybeMessage = data as {
      message?: string
      error?: string
      detail?: string
    }

    if (maybeMessage.message) {
      return maybeMessage.message
    }

    if (maybeMessage.detail) {
      return maybeMessage.detail
    }

    if (maybeMessage.error) {
      return maybeMessage.error
    }
  }

  if (status === 401) {
    return 'Hibas felhasznalonev vagy jelszo.'
  }

  if (status === 403) {
    return 'Nincs jogosultsagod ehhez a muvelethez.'
  }

  return error.message || 'Nem sikerult elerni a szervert.'
}
