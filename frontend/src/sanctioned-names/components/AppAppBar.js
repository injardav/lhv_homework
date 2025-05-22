import * as React from "react";
import { styled, alpha } from "@mui/material/styles";
import Box from "@mui/material/Box";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Button from "@mui/material/Button";
import { useLocation, Link as RouterLink } from "react-router-dom";

const StyledToolbar = styled(Toolbar)(({ theme }) => ({
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    flexShrink: 0,
    borderRadius: `calc(${theme.shape.borderRadius}px + 8px)`,
    backdropFilter: "blur(24px)",
    border: "1px solid",
    borderColor: (theme.vars || theme).palette.divider,
    backgroundColor: theme.vars
        ? `rgba(${theme.vars.palette.background.defaultChannel} / 0.4)`
        : alpha(theme.palette.background.default, 0.4),
    boxShadow: (theme.vars || theme).shadows[1],
    padding: "8px 12px",
}));

export default function AppAppBar() {
    const location = useLocation();

    const isActive = (path) => location.pathname === path;

    return (
        <AppBar
            position="fixed"
            enableColorOnDark
            sx={{
                boxShadow: 0,
                bgcolor: "transparent",
                backgroundImage: "none",
                mt: "calc(var(--template-frame-height, 0px) + 28px)",
            }}
        >
            <Box sx={{ display: "inline-flex", mx: "auto" }}>
                <StyledToolbar variant="dense" disableGutters>
                    <Box
                        sx={{
                            flexGrow: 1,
                            display: "flex",
                            alignItems: "center",
                            px: 0,
                        }}
                    >
                        <Box sx={{ display: { xs: "none", md: "flex" } }}>
                            <Button
                                component={RouterLink}
                                to="/"
                                variant="text"
                                color="info"
                                size="small"
                                sx={{
                                    borderBottom: isActive("/")
                                        ? "2px solid currentColor"
                                        : "none",
                                    borderRadius: 0,
                                    fontWeight: isActive("/")
                                        ? "bold"
                                        : "normal",
                                }}
                            >
                                Home
                            </Button>
                            <Button
                                component={RouterLink}
                                to="people"
                                variant="text"
                                color="info"
                                size="small"
                                sx={{
                                    borderBottom: isActive("/people")
                                        ? "2px solid currentColor"
                                        : "none",
                                    borderRadius: 0,
                                    fontWeight: isActive("/people")
                                        ? "bold"
                                        : "normal",
                                }}
                            >
                                People
                            </Button>
                        </Box>
                    </Box>
                </StyledToolbar>
            </Box>
        </AppBar>
    );
}
