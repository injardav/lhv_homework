import * as React from 'react';
import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import { IconButton, TextField, Button } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { DataGrid } from '@mui/x-data-grid';

export default function People() {
    const [rows, setRows] = React.useState([
    { id: 1, name: 'David' },
    { id: 2, name: 'Alice' },
  ]);
  const [newName, setNewName] = React.useState('');

  const handleEdit = (params) => {
    const updated = rows.map(row =>
      row.id === params.id ? { ...row, [params.field]: params.value } : row
    );
    setRows(updated);
  };

  const handleDelete = (id) => {
    setRows(rows.filter(row => row.id !== id));
  };

  const handleAdd = () => {
    if (!newName.trim()) return;
    const newId = rows.length ? Math.max(...rows.map(r => r.id)) + 1 : 1;
    setRows([...rows, { id: newId, name: newName }]);
    setNewName('');
  };

  const columns = [
    {
      field: 'name',
      headerName: 'Name',
      flex: 1,
      editable: true,
    },
    {
      field: 'actions',
      headerName: 'Actions',
      sortable: false,
      renderCell: (params) => (
        <IconButton onClick={() => handleDelete(params.id)}>
          <DeleteIcon />
        </IconButton>
      ),
    },
  ];
  
  return (
    <Box
      id="sanctionednames"
      sx={(theme) => ({
        width: '100%',
        backgroundRepeat: 'no-repeat',
        backgroundImage:
          'radial-gradient(ellipse 80% 50% at 50% -20%, hsl(210, 100%, 90%), transparent)',
        ...theme.applyStyles('dark', {
          backgroundImage:
            'radial-gradient(ellipse 80% 50% at 50% -20%, hsl(210, 100%, 16%), transparent)',
        }),
      })}
    >
      <Container
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          pt: { xs: 14, sm: 20 },
          pb: { xs: 8, sm: 12 },
        }}
      >
        asd
      </Container>
    </Box>
  );
};
