import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Collapse,
  Divider,
  IconButton,
  InputAdornment,
  LinearProgress,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import DirectionsCarFilledIcon from '@mui/icons-material/DirectionsCarFilled'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import HistoryIcon from '@mui/icons-material/History'
import Inventory2Icon from '@mui/icons-material/Inventory2'
import LogoutIcon from '@mui/icons-material/Logout'
import RefreshIcon from '@mui/icons-material/Refresh'
import SearchIcon from '@mui/icons-material/Search'
import UploadFileIcon from '@mui/icons-material/UploadFile'
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import {
  checkLogin,
  getApiErrorMessage,
  importVehicles,
  listVehicleImports,
  listVehicles,
  searchVehicles,
} from './api'
import type {
  AuthCredentials,
  Vehicle,
  VehicleImportHistoryItem,
  VehicleImportResult,
} from './types'

function App() {
  const queryClient = useQueryClient()
  const [auth, setAuth] = useState<AuthCredentials | null>(null)
  const [loginForm, setLoginForm] = useState({
    username: 'admin',
    password: '',
  })
  const [vinInput, setVinInput] = useState('')
  const [submittedVin, setSubmittedVin] = useState('')
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [lastImportResult, setLastImportResult] =
    useState<VehicleImportResult | null>(null)
  const [expandedImportId, setExpandedImportId] = useState<number | null>(null)

  const loginMutation = useMutation({
    mutationFn: checkLogin,
    onSuccess: (_data, credentials) => {
      setAuth(credentials)
      setLastImportResult(null)
    },
  })

  const vehiclesQuery = useQuery({
    queryKey: ['vehicles', submittedVin],
    enabled: Boolean(auth),
    queryFn: () => {
      if (!auth) {
        throw new Error('Nincs aktiv bejelentkezes.')
      }

      const normalizedVin = submittedVin.trim()

      if (normalizedVin) {
        return searchVehicles(auth, normalizedVin)
      }

      return listVehicles(auth)
    },
  })

  const importsQuery = useQuery({
    queryKey: ['vehicle-imports'],
    enabled: Boolean(auth),
    queryFn: () => {
      if (!auth) {
        throw new Error('Nincs aktiv bejelentkezes.')
      }

      return listVehicleImports(auth)
    },
  })

  const importMutation = useMutation({
    mutationFn: (file: File) => {
      if (!auth) {
        throw new Error('Nincs aktiv bejelentkezes.')
      }

      return importVehicles(auth, file)
    },
    onSuccess: (result) => {
      setLastImportResult(result)
      setSelectedFile(null)
      queryClient.invalidateQueries({ queryKey: ['vehicles'] })
      queryClient.invalidateQueries({ queryKey: ['vehicle-imports'] })
    },
  })

  const stats = useMemo(
    () => createStats(vehiclesQuery.data ?? [], importsQuery.data ?? []),
    [vehiclesQuery.data, importsQuery.data],
  )

  function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    loginMutation.mutate({
      username: loginForm.username.trim(),
      password: loginForm.password,
    })
  }

  function handleLogout() {
    setAuth(null)
    setLoginForm((current) => ({ ...current, password: '' }))
    setSubmittedVin('')
    setVinInput('')
    setLastImportResult(null)
    queryClient.clear()
  }

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmittedVin(vinInput.trim())
  }

  function handleRefresh() {
    queryClient.invalidateQueries({ queryKey: ['vehicles'] })
    queryClient.invalidateQueries({ queryKey: ['vehicle-imports'] })
  }

  if (!auth) {
    return (
      <Box className="app-shell login-shell">
        <Card className="login-card" variant="outlined">
          <CardContent>
            <Stack spacing={3}>
              <Stack spacing={1}>
                <Box className="brand-mark">
                  <Inventory2Icon />
                </Box>
                <Typography variant="h4" component="h1">
                  SmartInventory
                </Typography>
                <Typography color="text.secondary">
                  Jelentkezz be az admin felulet eleresehez.
                </Typography>
              </Stack>

              {loginMutation.isError ? (
                <Alert severity="error">
                  {getApiErrorMessage(loginMutation.error)}
                </Alert>
              ) : null}

              <Box component="form" onSubmit={handleLogin}>
                <Stack spacing={2}>
                  <TextField
                    label="Felhasznalonev"
                    value={loginForm.username}
                    autoComplete="username"
                    onChange={(event) =>
                      setLoginForm((current) => ({
                        ...current,
                        username: event.target.value,
                      }))
                    }
                    required
                    fullWidth
                  />
                  <TextField
                    label="Jelszo"
                    value={loginForm.password}
                    type="password"
                    autoComplete="current-password"
                    onChange={(event) =>
                      setLoginForm((current) => ({
                        ...current,
                        password: event.target.value,
                      }))
                    }
                    required
                    fullWidth
                  />
                  <Button
                    type="submit"
                    size="large"
                    variant="contained"
                    disabled={loginMutation.isPending}
                    startIcon={
                      loginMutation.isPending ? (
                        <CircularProgress color="inherit" size={18} />
                      ) : (
                        <VerifiedUserIcon />
                      )
                    }
                  >
                    Belepes
                  </Button>
                </Stack>
              </Box>
            </Stack>
          </CardContent>
        </Card>
      </Box>
    )
  }

  return (
    <Box className="app-shell">
      <Box component="header" className="topbar">
        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
          <Box className="brand-mark compact">
            <Inventory2Icon />
          </Box>
          <Box>
            <Typography variant="h5" component="h1">
              SmartInventory
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Jarmu keszlet es import admin
            </Typography>
          </Box>
        </Stack>

        <Stack direction="row" spacing={1}>
          <Tooltip title="Adatok frissitese">
            <IconButton onClick={handleRefresh} color="primary">
              <RefreshIcon />
            </IconButton>
          </Tooltip>
          <Button
            variant="outlined"
            startIcon={<LogoutIcon />}
            onClick={handleLogout}
          >
            Kilepes
          </Button>
        </Stack>
      </Box>

      <Box className="content-grid">
        <Stack spacing={2.5} className="main-column">
          <Box className="stat-grid">
            {stats.map((stat) => (
              <Card variant="outlined" key={stat.label}>
                <CardContent>
                  <Stack spacing={1}>
                    <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                      {stat.icon}
                      <Typography variant="body2" color="text.secondary">
                        {stat.label}
                      </Typography>
                    </Stack>
                    <Typography variant="h4">{stat.value}</Typography>
                  </Stack>
                </CardContent>
              </Card>
            ))}
          </Box>

          <Card variant="outlined">
            <CardContent>
              <Stack spacing={2}>
                <Stack
                  direction={{ xs: 'column', sm: 'row' }}
                  spacing={2}
                  sx={{ justifyContent: 'space-between' }}
                >
                  <Box>
                    <Typography variant="h6">Jarmuvek</Typography>
                    <Typography variant="body2" color="text.secondary">
                      Teljes lista vagy VIN alapu kereses.
                    </Typography>
                  </Box>
                  <Box component="form" onSubmit={handleSearch}>
                    <Stack direction="row" spacing={1}>
                      <TextField
                        size="small"
                        placeholder="VIN kereses"
                        value={vinInput}
                        onChange={(event) => setVinInput(event.target.value)}
                        slotProps={{
                          input: {
                            startAdornment: (
                              <InputAdornment position="start">
                                <SearchIcon fontSize="small" />
                              </InputAdornment>
                            ),
                          },
                        }}
                      />
                      <Button type="submit" variant="contained">
                        Kereses
                      </Button>
                    </Stack>
                  </Box>
                </Stack>

                {vehiclesQuery.isFetching ? <LinearProgress /> : null}

                {vehiclesQuery.isError ? (
                  <Alert severity="error">
                    {getApiErrorMessage(vehiclesQuery.error)}
                  </Alert>
                ) : (
                  <VehicleTable vehicles={vehiclesQuery.data ?? []} />
                )}
              </Stack>
            </CardContent>
          </Card>
        </Stack>

        <Stack spacing={2.5} className="side-column">
          <Card variant="outlined">
            <CardContent>
              <Stack spacing={2}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                  <UploadFileIcon color="primary" />
                  <Box>
                    <Typography variant="h6">Excel import</Typography>
                    <Typography variant="body2" color="text.secondary">
                      .xlsx jarmu keszlet feltoltese.
                    </Typography>
                  </Box>
                </Stack>

                <Button
                  component="label"
                  variant="outlined"
                  startIcon={<UploadFileIcon />}
                >
                  Fajl kivalasztasa
                  <input
                    type="file"
                    accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    hidden
                    onChange={(event) =>
                      setSelectedFile(event.target.files?.[0] ?? null)
                    }
                  />
                </Button>

                {selectedFile ? (
                  <Chip
                    label={selectedFile.name}
                    onDelete={() => setSelectedFile(null)}
                    color="secondary"
                    variant="outlined"
                  />
                ) : null}

                <Button
                  variant="contained"
                  disabled={!selectedFile || importMutation.isPending}
                  onClick={() => selectedFile && importMutation.mutate(selectedFile)}
                  startIcon={
                    importMutation.isPending ? (
                      <CircularProgress color="inherit" size={18} />
                    ) : (
                      <UploadFileIcon />
                    )
                  }
                >
                  Import inditasa
                </Button>

                {importMutation.isError ? (
                  <Alert severity="error">
                    {getApiErrorMessage(importMutation.error)}
                  </Alert>
                ) : null}

                {lastImportResult ? (
                  <ImportResultSummary result={lastImportResult} />
                ) : null}
              </Stack>
            </CardContent>
          </Card>

          <Card variant="outlined">
            <CardContent>
              <Stack spacing={2}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                  <HistoryIcon color="primary" />
                  <Box>
                    <Typography variant="h6">Import history</Typography>
                    <Typography variant="body2" color="text.secondary">
                      Legutobbi feltoltesek es sorhibak.
                    </Typography>
                  </Box>
                </Stack>

                {importsQuery.isFetching ? <LinearProgress /> : null}

                {importsQuery.isError ? (
                  <Alert severity="error">
                    {getApiErrorMessage(importsQuery.error)}
                  </Alert>
                ) : (
                  <ImportHistory
                    items={importsQuery.data ?? []}
                    expandedImportId={expandedImportId}
                    onToggleExpanded={(id) =>
                      setExpandedImportId((current) =>
                        current === id ? null : id,
                      )
                    }
                  />
                )}
              </Stack>
            </CardContent>
          </Card>
        </Stack>
      </Box>
    </Box>
  )
}

function VehicleTable({ vehicles }: { vehicles: Vehicle[] }) {
  if (vehicles.length === 0) {
    return <Alert severity="info">Nincs megjelenitheto jarmu.</Alert>
  }

  return (
    <TableContainer>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>VIN</TableCell>
            <TableCell>Marka / tipus</TableCell>
            <TableCell>Rendszam</TableCell>
            <TableCell>Beszerzes</TableCell>
            <TableCell align="right">Koltseg osszesen</TableCell>
            <TableCell>Statusz</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {vehicles.map((vehicle) => (
            <TableRow key={vehicle.id} hover>
              <TableCell>
                <Typography sx={{ fontFamily: 'monospace', fontSize: 13 }}>
                  {vehicle.vin}
                </Typography>
              </TableCell>
              <TableCell>
                <Stack spacing={0.25}>
                  <Typography sx={{ fontWeight: 700 }}>
                    {vehicle.brand ?? 'Ismeretlen'}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {vehicle.typeName ?? '-'}
                  </Typography>
                </Stack>
              </TableCell>
              <TableCell>{vehicle.registrationNumber ?? '-'}</TableCell>
              <TableCell>{formatDate(vehicle.acquisitionDate)}</TableCell>
              <TableCell align="right">
                {formatCurrency(vehicle.totalCostAmount)}
              </TableCell>
              <TableCell>
                <Chip
                  size="small"
                  label={vehicle.status}
                  color={vehicle.status === 'IMPORTED' ? 'primary' : 'default'}
                  variant="outlined"
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}

function ImportResultSummary({ result }: { result: VehicleImportResult }) {
  return (
    <Alert
      severity={result.rejectedRows > 0 ? 'warning' : 'success'}
      variant="outlined"
    >
      <Stack spacing={1}>
        <Typography sx={{ fontWeight: 800 }}>{result.originalFilename}</Typography>
        <Typography variant="body2">
          Osszes sor: {result.totalRows}, beszurva: {result.insertedRows},
          kihagyva: {result.skippedRows}, hibas: {result.rejectedRows}
        </Typography>
        {result.errors.length > 0 ? (
          <Stack spacing={0.5}>
            {result.errors.map((error) => (
              <Typography
                key={`${error.sourceRowNumber}-${error.message}`}
                variant="body2"
              >
                {error.sourceRowNumber}. sor: {error.message}
              </Typography>
            ))}
          </Stack>
        ) : null}
      </Stack>
    </Alert>
  )
}

function ImportHistory({
  items,
  expandedImportId,
  onToggleExpanded,
}: {
  items: VehicleImportHistoryItem[]
  expandedImportId: number | null
  onToggleExpanded: (id: number) => void
}) {
  if (items.length === 0) {
    return <Alert severity="info">Meg nincs import elozyemeny.</Alert>
  }

  return (
    <Stack spacing={1.25}>
      {items.map((item) => {
        const isExpanded = expandedImportId === item.id

        return (
          <Box className="history-item" key={item.id}>
            <Stack spacing={1}>
              <Stack
                direction="row"
                spacing={1}
                sx={{ justifyContent: 'space-between' }}
              >
                <Box sx={{ minWidth: 0 }}>
                  <Typography sx={{ fontWeight: 800 }} noWrap>
                    {item.originalFilename}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {formatDateTime(item.importedAt)}
                  </Typography>
                </Box>
                <Tooltip title="Sorhibak mutatasa">
                  <span>
                    <IconButton
                      size="small"
                      disabled={item.errors.length === 0}
                      onClick={() => onToggleExpanded(item.id)}
                    >
                      <ExpandMoreIcon
                        className={
                          isExpanded ? 'rotate-icon expanded' : 'rotate-icon'
                        }
                      />
                    </IconButton>
                  </span>
                </Tooltip>
              </Stack>

              <Stack direction="row" sx={{ flexWrap: 'wrap', gap: 1 }}>
                <Chip size="small" label={`Osszes: ${item.totalRows}`} />
                <Chip
                  size="small"
                  color="primary"
                  label={`Uj: ${item.insertedRows}`}
                />
                <Chip size="small" label={`Dupla: ${item.skippedRows}`} />
                <Chip
                  size="small"
                  color={item.rejectedRows > 0 ? 'warning' : 'default'}
                  label={`Hibas: ${item.rejectedRows}`}
                />
              </Stack>

              <Collapse in={isExpanded}>
                <Divider sx={{ my: 1 }} />
                <Stack spacing={0.75}>
                  {item.errors.map((error) => (
                    <Alert
                      key={`${item.id}-${error.sourceRowNumber}-${error.message}`}
                      severity="warning"
                      variant="outlined"
                    >
                      {error.sourceRowNumber}. sor: {error.message}
                    </Alert>
                  ))}
                </Stack>
              </Collapse>
            </Stack>
          </Box>
        )
      })}
    </Stack>
  )
}

function createStats(
  vehicles: Vehicle[],
  imports: VehicleImportHistoryItem[],
) {
  const rejectedRows = imports.reduce(
    (total, item) => total + item.rejectedRows,
    0,
  )
  const insertedRows = imports.reduce(
    (total, item) => total + item.insertedRows,
    0,
  )

  return [
    {
      label: 'Jarmuvek',
      value: vehicles.length,
      icon: <DirectionsCarFilledIcon color="primary" fontSize="small" />,
    },
    {
      label: 'Importok',
      value: imports.length,
      icon: <HistoryIcon color="secondary" fontSize="small" />,
    },
    {
      label: 'Beszurt sorok',
      value: insertedRows,
      icon: <UploadFileIcon color="primary" fontSize="small" />,
    },
    {
      label: 'Hibas sorok',
      value: rejectedRows,
      icon: <Inventory2Icon color="warning" fontSize="small" />,
    },
  ]
}

function formatDate(value: string | null) {
  if (!value) {
    return '-'
  }

  return new Intl.DateTimeFormat('hu-HU').format(new Date(value))
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('hu-HU', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

function formatCurrency(value: number | null | undefined) {
  if (value == null) {
    return '-'
  }

  return new Intl.NumberFormat('hu-HU', {
    currency: 'HUF',
    maximumFractionDigits: 0,
    style: 'currency',
  }).format(value)
}

export default App
