import { useEffect, useState } from 'react';
import { Grid, Paper, Typography, Box } from '@mui/material';
import { People, MenuBook, Quiz, Assessment } from '@mui/icons-material';
import { jadidService } from '../services/jadidService';
import { bookService } from '../services/bookService';
import { testService } from '../services/testService';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';

interface Stats {
  jadidsCount: number;
  booksCount: number;
  testsCount: number;
  resultsCount: number;
}

export default function Dashboard() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadStats = async () => {
    console.log('[Dashboard] loadStats() - Начало загрузки статистики');
    try {
      setLoading(true);
      setError('');
      console.log('[Dashboard] loadStats() - Запрос данных из всех сервисов...');
      const [jadids, books, tests, results] = await Promise.all([
        jadidService.getAll(),
        bookService.getAll(),
        testService.getAll(),
        testService.getResults()
      ]);
      const statsData = {
        jadidsCount: jadids.length,
        booksCount: books.length,
        testsCount: tests.length,
        resultsCount: results.length
      };
      console.log('[Dashboard] loadStats() - Статистика успешно загружена:', statsData);
      setStats(statsData);
    } catch (err) {
      console.error('[Dashboard] loadStats() - ОШИБКА:', err);
      console.error('[Dashboard] loadStats() - Детали ошибки:', {
        message: err instanceof Error ? err.message : String(err),
        stack: err instanceof Error ? err.stack : undefined
      });
      setError('Ma\'lumotlarni yuklashda xatolik yuz berdi');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadStats();
  }, []);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} onRetry={loadStats} />;
  if (!stats) return null;

  const cards = [
    { title: 'Jadidlar', count: stats.jadidsCount, icon: <People sx={{ fontSize: 48 }} />, color: '#1976d2' },
    { title: 'Kitoblar', count: stats.booksCount, icon: <MenuBook sx={{ fontSize: 48 }} />, color: '#2e7d32' },
    { title: 'Testlar', count: stats.testsCount, icon: <Quiz sx={{ fontSize: 48 }} />, color: '#ed6c02' },
    { title: 'Natijalar', count: stats.resultsCount, icon: <Assessment sx={{ fontSize: 48 }} />, color: '#9c27b0' }
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>
      <Grid container spacing={3}>
        {cards.map((card) => (
          <Grid item xs={12} sm={6} md={3} key={card.title}>
            <Paper
              sx={{
                p: 3,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                bgcolor: card.color,
                color: 'white'
              }}
            >
              {card.icon}
              <Typography variant="h3" sx={{ mt: 2 }}>
                {card.count}
              </Typography>
              <Typography variant="h6">{card.title}</Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
