import React, { useEffect, useState } from "react";
import { Box, Container, TextField, Button, IconButton } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import { DataGrid } from "@mui/x-data-grid";

export default function People() {
    const [people, setPeople] = useState([]);
    const [newName, setNewName] = useState("");
    const [error, setError] = useState(false);

    const addPerson = async (name) => {
        if (error || !newName.trim()) return;

        try {
            const res = await fetch("http://localhost:8080/api/v1/names", {
                method: "POST",
                headers: { "Content-Type": "text/plain" },
                body: name,
            });
            if (!res.ok) throw new Error("Failed to add person");
            const newPerson = await res.json();
            setPeople((prev) => [...prev, newPerson]);
            setNewName("");
            setError(false);
        } catch (err) {
            console.error(err);
        }
    };

    const deletePerson = async (id) => {
        try {
            const res = await fetch(
                `http://localhost:8080/api/v1/names/${id}`,
                {
                    method: "DELETE",
                }
            );
            if (!res.ok) throw new Error("Failed to delete person");
            setPeople((prev) => prev.filter((person) => person.id !== id));
        } catch (err) {
            console.error(err);
        }
    };

    const updatePerson = async (id, newName) => {
        const res = await fetch(`http://localhost:8080/api/v1/names/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "text/plain" },
            body: newName,
        });
        if (!res.ok) throw new Error("Failed to update person");
        const updatedPerson = await res.json();
        setPeople((prev) =>
            prev.map((person) => (person.id === id ? updatedPerson : person))
        );
    };

    const processRowUpdate = async (newRow) => {
        const oldRow = people.find((p) => p.id === newRow.id);

        try {
            await updatePerson(newRow.id, newRow.name);
            return newRow;
        } catch (err) {
            console.error("Failed to update:", err);
            console.log("Reverting to old row:", oldRow);
            return oldRow;
        }
    };

    const handleChange = (event) => {
        const value = event.target.value;
        setNewName(value);
        setError(!/^(?=.*[A-Za-z])[A-Za-z\s-]{2,50}$/.test(value));
    };

    const validateNameFormat = (name) => {
        const valid = /^(?=.*[A-Za-z])[A-Za-z\s-]{2,50}$/.test(name);
        return valid
            ? null
            : "Invalid name: must contain letters, 2–50 characters, letters/spaces/dashes only.";
    };

    useEffect(() => {
        const fetchPeople = async () => {
            try {
                const res = await fetch("http://localhost:8080/api/v1/names");
                if (!res.ok) throw new Error("Failed to fetch people");
                const data = await res.json();
                setPeople(data);
            } catch (err) {
                console.error("Error fetching people:", err);
            }
        };

        fetchPeople();
    }, []);

    const columns = [
        {
            field: "id",
            headerName: "ID",
            width: 100,
        },
        {
            field: "name",
            headerName: "Name",
            flex: 1,
            editable: true,
        },
        {
            field: "delete",
            headerName: "",
            width: 80,
            sortable: false,
            renderCell: (params) => (
                <IconButton
                    onClick={() => deletePerson(params.row.id)}
                    color="error"
                    size="small"
                >
                    <DeleteIcon fontSize="small" />
                </IconButton>
            ),
        },
    ];

    return (
        <Box
            id="sanctionednames"
            sx={(theme) => ({
                width: "100%",
                backgroundRepeat: "no-repeat",
                backgroundImage:
                    "radial-gradient(ellipse 80% 50% at 50% -20%, hsl(210, 100%, 90%), transparent)",
                ...theme.applyStyles("dark", {
                    backgroundImage:
                        "radial-gradient(ellipse 80% 50% at 50% -20%, hsl(210, 100%, 16%), transparent)",
                }),
            })}
        >
            <Container
                sx={{
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    pt: { xs: 14, sm: 20 },
                    pb: { xs: 8, sm: 12 },
                }}
            >
                <Box sx={{ display: "flex", gap: 1, mb: 2 }}>
                    <TextField
                        id="name_input"
                        hiddenLabel
                        size="small"
                        variant="outlined"
                        aria-label="New person name"
                        label="New person name"
                        fullwidth
                        value={newName}
                        onChange={handleChange}
                        error={error}
                        helperText={
                            error
                                ? "Name must contain letters and only letters, dashes, or spaces (max 50 characters)."
                                : " "
                        }
                        slotProps={{
                            htmlInput: {
                                autoComplete: "off",
                                "aria-label": "New person name",
                            },
                        }}
                    />
                    <Button
                        variant="contained"
                        color="primary"
                        size="small"
                        sx={{ minWidth: "fit-content" }}
                        onClick={() => addPerson(newName)}
                    >
                        Add
                    </Button>
                </Box>

                <Box sx={{ height: 500, width: "100%" }}>
                    <DataGrid
                        rows={people}
                        columns={columns}
                        disableSelectionOnClick
                        processRowUpdate={processRowUpdate}
                        pageSize={5}
                        rowsPerPageOptions={[5]}
                        experimentalFeatures={{ newEditingApi: true }}
                    />
                </Box>
            </Container>
        </Box>
    );
}
