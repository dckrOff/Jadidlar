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
  Typography,
  Avatar
} from '@mui/material';
import { Add, Edit, Delete } from '@mui/icons-material';
import { bookService } from '../services/bookService';
import { Book } from '../types';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import ConfirmDialog from '../components/ConfirmDialog';
import BookDialog from './BookDialog';

export default function BooksPage() {
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingBook, setEditingBook] = useState<Book | null>(null);
  const [deleteDialog, setDeleteDialog] = useState<{ open: boolean; id: string }>({ open: false, id: '' });

  const loadBooks = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await bookService.getAll();
      setBooks(data);
    } catch (err) {
      setError('Ma\'lumotlarni yuklashda xatolik yuz berdi');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBooks();
  }, []);

  const handleAdd = () => {
    setEditingBook(null);
    setDialogOpen(true);
  };

  const handleEdit = (book: Book) => {
    setEditingBook(book);
    setDialogOpen(true);
  };

  const handleDelete = async () => {
    try {
      await bookService.delete(deleteDialog.id);
      setDeleteDialog({ open: false, id: '' });
      loadBooks();
    } catch (err) {
      setError('O\'chirishda xatolik yuz berdi');
    }
  };

  const handleSave = () => {
    setDialogOpen(false);
    loadBooks();
  };

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} onRetry={loadBooks} />;

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Kitoblar</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={handleAdd}>
          Qo'shish
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Muqova</TableCell>
              <TableCell>Nomi</TableCell>
              <TableCell>Muallif</TableCell>
              <TableCell>Yil</TableCell>
              <TableCell>Reyting</TableCell>
              <TableCell>Tartib</TableCell>
              <TableCell align="right">Amallar</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {books.map((book) => (
              <TableRow key={book.id}>
                <TableCell>
                  <Avatar variant="rounded" src={book.coverImageUrl} alt={book.title} />
                </TableCell>
                <TableCell>{book.title}</TableCell>
                <TableCell>{book.authorName}</TableCell>
                <TableCell>{book.publishYear}</TableCell>
                <TableCell>{book.rating}</TableCell>
                <TableCell>{book.orderIndex}</TableCell>
                <TableCell align="right">
                  <IconButton onClick={() => handleEdit(book)}>
                    <Edit />
                  </IconButton>
                  <IconButton onClick={() => setDeleteDialog({ open: true, id: book.id })}>
                    <Delete />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <BookDialog
        open={dialogOpen}
        book={editingBook}
        onClose={() => setDialogOpen(false)}
        onSave={handleSave}
      />

      <ConfirmDialog
        open={deleteDialog.open}
        title="Kitobni o'chirish"
        message="Ushbu kitobni o'chirishni xohlaysizmi? Bu amalni qaytarib bo'lmaydi."
        onConfirm={handleDelete}
        onCancel={() => setDeleteDialog({ open: false, id: '' })}
      />
    </Box>
  );
}
