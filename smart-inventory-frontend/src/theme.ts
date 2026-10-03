import { createTheme } from '@mui/material/styles'

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#216b5c',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#5d5f9f',
    },
    warning: {
      main: '#b0692f',
    },
    background: {
      default: '#f5f7f6',
      paper: '#ffffff',
    },
    text: {
      primary: '#1f2926',
      secondary: '#62706b',
    },
    divider: '#dce4df',
  },
  shape: {
    borderRadius: 8,
  },
  typography: {
    fontFamily:
      'Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    h1: {
      fontWeight: 760,
      letterSpacing: 0,
    },
    h2: {
      fontWeight: 740,
      letterSpacing: 0,
    },
    h3: {
      fontWeight: 720,
      letterSpacing: 0,
    },
    button: {
      fontWeight: 700,
      letterSpacing: 0,
      textTransform: 'none',
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderColor: '#dce4df',
          boxShadow: '0 10px 30px rgba(31, 41, 38, 0.07)',
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 6,
          fontWeight: 700,
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          color: '#62706b',
          fontSize: 12,
          fontWeight: 800,
          letterSpacing: 0,
          textTransform: 'uppercase',
        },
      },
    },
  },
})
