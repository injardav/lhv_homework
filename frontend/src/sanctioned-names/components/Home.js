import React from "react";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Container from "@mui/material/Container";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

export default function Home() {
    const [name, setName] = React.useState("");
    const [error, setError] = React.useState(false);

    const handleChange = (event) => {
        const value = event.target.value;
        setName(value);
        setError(!/^(?=.*[A-Za-z])[A-Za-z\s,.-]{2,50}$/.test(value));
    };

    const verifyName = async (name) => {
        if (error || !name.trim()) return;

        try {
            const res = await fetch(
                "http://localhost:8080/api/v1/names/verify",
                {
                    method: "POST",
                    headers: { "Content-Type": "text/plain" },
                    body: name,
                }
            );
            if (!res.ok) throw new Error("Failed to verify name");
            const result = await res.json();
            if (result.isSanctioned) {
                alert(
                    `
Name was similar to: ${result.sanctionedName}\n
Jaro-Winkler similarity: ${result.jaro.toFixed(3)}\n
Jaccard similarity: ${result.jaccard.toFixed(3)}\n
Levenshtein distance norm: ${result.levenshteinNorm.toFixed(3)}\n
Phonetic matches: ${result.phoneticMatches}
`
                );
            } else {
                alert(`Verification result: ${result.msg}`);
            }
        } catch (err) {
            console.error(err);
        }
    };

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
                <Stack
                    spacing={2}
                    useFlexGap
                    sx={{
                        alignItems: "center",
                        width: { xs: "100%", sm: "70%" },
                    }}
                >
                    <Typography
                        variant="h1"
                        sx={{
                            display: "flex",
                            flexDirection: { xs: "column", sm: "row" },
                            alignItems: "center",
                            fontSize: "clamp(3rem, 10vw, 3.5rem)",
                        }}
                    >
                        Verify&nbsp;a&nbsp;
                        <Typography
                            component="span"
                            variant="h1"
                            sx={(theme) => ({
                                fontSize: "inherit",
                                color: "primary.main",
                                ...theme.applyStyles("dark", {
                                    color: "primary.light",
                                }),
                            })}
                        >
                            person
                        </Typography>
                    </Typography>
                    <Typography
                        sx={{
                            textAlign: "center",
                            color: "text.secondary",
                            width: { sm: "100%", md: "80%" },
                        }}
                    >
                        Banks are responsible for stopping money laundering.
                        Hence it is necessary to avoid money transfers to
                        terrorists and criminals. Write a name to verify it
                        against a list of sanctioned people to detect suspicious
                        transfers.
                    </Typography>
                    <Stack
                        direction={{ xs: "column", sm: "row" }}
                        spacing={1}
                        useFlexGap
                        sx={{ pt: 2, width: { xs: "100%", sm: "350px" } }}
                    >
                        <TextField
                            id="name_input"
                            hiddenLabel
                            size="small"
                            variant="outlined"
                            aria-label="Enter the name of the person"
                            placeholder="Name of the person"
                            fullWidth
                            value={name}
                            onChange={handleChange}
                            error={error}
                            helperText={
                                error
                                    ? "Name must contain letters and only letters, dashes, or spaces (max 50 characters)."
                                    : " "
                            }
                            onKeyDown={(e) => {
                                if (e.key === "Enter") {
                                    e.preventDefault();
                                    verifyName(name);
                                }
                            }}
                            slotProps={{
                                htmlInput: {
                                    autoComplete: "off",
                                    "aria-label":
                                        "Enter the name of the person",
                                },
                            }}
                        />
                        <Button
                            variant="contained"
                            color="primary"
                            size="small"
                            sx={{ minWidth: "fit-content" }}
                            onClick={() => verifyName(name)}
                        >
                            Check
                        </Button>
                    </Stack>
                </Stack>
            </Container>
        </Box>
    );
}
