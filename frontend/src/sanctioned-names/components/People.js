import React, { useEffect, useState } from 'react';
import {
  Box,
  Container,
  TextField,
  Button,
  IconButton,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { DataGrid } from '@mui/x-data-grid';

export default function People() {
    const [people, setPeople] = React.useState([]);
    const [newName, setNewName] = useState('');
    
    const addPerson = async (name) => {
        const res = await fetch('http://localhost:8080/api/v1/names', {
            method: 'POST',
            headers: {'Content-Type': 'text/plain'},
            body: name
        });
        if (!res.ok) throw new Error('Failed to add person');
        const newPerson = await res.json();
        setPeople((prev) => [...prev, newPerson]);
    };

    const deletePerson = async (id) => {
        const res = await fetch(`http://localhost:8080/api/v1/names/${id}`, {
            method: 'DELETE'
        });
        if (!res.ok) throw new Error('Failed to delete person');
        setPeople((prev) => prev.filter((person) => person.id !== id));
    };

    const updatePerson = async (id, newName) => {
        const res = await fetch(`http://localhost:8080/api/v1/names/${id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'text/plain'},
            body: newName
        });
        if (!res.ok) throw new Error('Failed to update person');
        const updatedPerson = await res.json();
        setPeople((prev) => prev.map((person) => (person.id === id ? updatedPerson : person)));
    };

    const handleEditCommit = (params) => {
        if (params.field === 'name') {
        updatePerson(params.id, params.value);
        }
    };

    useEffect(() => {
        fetch('http://localhost:8080/api/v1/names')
            .then(res => res.json())
            .then(data => setPeople(data))
            .catch(err => console.error(err));
        }, []);

    const columns = [
        {
        field: 'id',
        headerName: 'ID',
        width: 100,
        },
        {
        field: 'name',
        headerName: 'Name',
        flex: 1,
        editable: true,
        },
        {
        field: 'actions',
        headerName: 'Actions',
        width: 100,
        sortable: false,
        renderCell: (params) => (
            <IconButton onClick={() => deletePerson(params.id)}>
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
            <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                <TextField
                    id="name_input"
                    hiddenLabel
                    size="small"
                    variant="outlined"
                    aria-label="New person name"
                    label="New person name"
                    fullwidth
                    value={newName}
                    onChange={(e) => setNewName(e.target.value)}
                    slotProps={{
                        htmlInput: {
                        autoComplete: 'off',
                        'aria-label': 'New person name',
                        },
                    }}
                />
                <Button variant="contained"
                    color="primary"
                    size="small"
                    sx={{ minWidth: 'fit-content' }}
                    onClick={addPerson}
                >
                    Add
                </Button>
            </Box>

            <Box sx={{ height: 500, width: '100%' }}>
            {console.log('People rows:', people)}
            <DataGrid
                rows={people}
                columns={columns}
                disableSelectionOnClick
                onCellEditCommit={handleEditCommit}
                pageSize={5}
                rowsPerPageOptions={[5]}
            />
            </Box>
        </Container>
        </Box>
    );
};
