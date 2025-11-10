import { useEffect, useState } from 'react';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Chip
} from '@mui/material';
import { testService } from '../services/testService';
import { TestResult } from '../types';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import { formatDate, formatTimeSpent } from '../utils/formatters';

export default function ResultsPage() {
  const [results, setResults] = useState<TestResult[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadResults = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await testService.getResults();
      setResults(data);
    } catch (err) {
      setError('Ma\'lumotlarni yuklashda xatolik yuz berdi');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadResults();
  }, []);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} onRetry={loadResults} />;

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Test natijalari
      </Typography>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Foydalanuvchi ID</TableCell>
              <TableCell>Test ID</TableCell>
              <TableCell>Ball</TableCell>
              <TableCell>Jami savollar</TableCell>
              <TableCell>Foiz</TableCell>
              <TableCell>Vaqt</TableCell>
              <TableCell>Sana</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {results.map((result) => {
              const percentage = Math.round((result.score / result.totalQuestions) * 100);
              const color = percentage >= 70 ? 'success' : percentage >= 50 ? 'warning' : 'error';
              
              return (
                <TableRow key={result.id}>
                  <TableCell>{result.userId}</TableCell>
                  <TableCell>{result.testId}</TableCell>
                  <TableCell>{result.score}</TableCell>
                  <TableCell>{result.totalQuestions}</TableCell>
                  <TableCell>
                    <Chip label={`${percentage}%`} color={color} size="small" />
                  </TableCell>
                  <TableCell>{formatTimeSpent(result.timeSpent)}</TableCell>
                  <TableCell>{formatDate(result.completedAt)}</TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
