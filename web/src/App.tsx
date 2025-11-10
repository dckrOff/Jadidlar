import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import JadidsPage from './pages/JadidsPage';
import BooksPage from './pages/BooksPage';
import TestsPage from './pages/TestsPage';
import ResultsPage from './pages/ResultsPage';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2'
    },
    secondary: {
      main: '#dc004e'
    }
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif'
  }
});

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<Dashboard />} />
            <Route path="jadids" element={<JadidsPage />} />
            <Route path="books" element={<BooksPage />} />
            <Route path="tests" element={<TestsPage />} />
            <Route path="results" element={<ResultsPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}
