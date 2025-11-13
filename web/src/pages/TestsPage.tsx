import { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Typography
} from '@mui/material';
import { Add, Edit, Delete } from '@mui/icons-material';
import { testService } from '../services/testService';
import { Test } from '../types';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import ConfirmDialog from '../components/ConfirmDialog';
import TestDialog from './TestDialog';

export default function TestsPage() {
  const [tests, setTests] = useState<Test[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingTest, setEditingTest] = useState<Test | null>(null);
  const [deleteDialog, setDeleteDialog] = useState<{ open: boolean; id: string }>({ open: false, id: '' });

  const loadTests = async () => {
    console.log('[TestsPage] loadTests() - Начало загрузки');
    try {
      setLoading(true);
      setError('');
      const data = await testService.getAll();
      console.log(`[TestsPage] loadTests() - Успешно загружено ${data.length} тестов`);
      setTests(data);
    } catch (err) {
      console.error('[TestsPage] loadTests() - ОШИБКА:', err);
      console.error('[TestsPage] loadTests() - Детали ошибки:', {
        message: err instanceof Error ? err.message : String(err),
        stack: err instanceof Error ? err.stack : undefined
      });
      setError('Ma\'lumotlarni yuklashda xatolik yuz berdi');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTests();
  }, []);

  const handleAdd = () => {
    setEditingTest(null);
    setDialogOpen(true);
  };

  const handleEdit = (test: Test) => {
    setEditingTest(test);
    setDialogOpen(true);
  };

  const handleDelete = async () => {
    console.log(`[TestsPage] handleDelete() - Начало удаления теста с ID: ${deleteDialog.id}`);
    try {
      await testService.delete(deleteDialog.id);
      console.log(`[TestsPage] handleDelete() - Тест успешно удален`);
      setDeleteDialog({ open: false, id: '' });
      loadTests();
    } catch (err) {
      console.error(`[TestsPage] handleDelete() - ОШИБКА при удалении:`, err);
      console.error(`[TestsPage] handleDelete() - Детали ошибки:`, {
        message: err instanceof Error ? err.message : String(err),
        stack: err instanceof Error ? err.stack : undefined,
        id: deleteDialog.id
      });
      setError('O\'chirishda xatolik yuz berdi');
    }
  };

  const handleSave = () => {
    setDialogOpen(false);
    loadTests();
  };

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} onRetry={loadTests} />;

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Testlar</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={handleAdd}>
          Qo'shish
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Nomi</TableCell>
              <TableCell>Tavsif</TableCell>
              <TableCell>Savollar soni</TableCell>
              <TableCell>Vaqt (daqiqa)</TableCell>
              <TableCell align="right">Amallar</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {tests.map((test) => (
              <TableRow key={test.id}>
                <TableCell>{test.title}</TableCell>
                <TableCell>{test.description}</TableCell>
                <TableCell>{test.questionCount}</TableCell>
                <TableCell>{test.timeLimit}</TableCell>
                <TableCell align="right">
                  <IconButton onClick={() => handleEdit(test)}>
                    <Edit />
                  </IconButton>
                  <IconButton onClick={() => setDeleteDialog({ open: true, id: test.id })}>
                    <Delete />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <TestDialog
        open={dialogOpen}
        test={editingTest}
        onClose={() => setDialogOpen(false)}
        onSave={handleSave}
      />

      <ConfirmDialog
        open={deleteDialog.open}
        title="Testni o'chirish"
        message="Ushbu testni o'chirishni xohlaysizmi? Bu amalni qaytarib bo'lmaydi."
        onConfirm={handleDelete}
        onCancel={() => setDeleteDialog({ open: false, id: '' })}
      />
    </Box>
  );
}
