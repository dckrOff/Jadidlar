import { Box, Alert, Button } from '@mui/material';

interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
}

export default function ErrorMessage({ message, onRetry }: ErrorMessageProps) {
  return (
    <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
      <Alert
        severity="error"
        action={
          onRetry && (
            <Button color="inherit" size="small" onClick={onRetry}>
              Qayta urinish
            </Button>
          )
        }
      >
        {message}
      </Alert>
    </Box>
  );
}
