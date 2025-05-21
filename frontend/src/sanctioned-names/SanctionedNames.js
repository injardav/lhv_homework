import React from 'react';
import { Routes, Route } from 'react-router-dom';
import CssBaseline from '@mui/material/CssBaseline';
import AppTheme from '../shared-theme/AppTheme';
import AppAppBar from './components/AppAppBar';
import Home from './components/Home';
import People from './components/People';

export default function SanctionedNames(props) {
  return (
    <AppTheme {...props}>
      <CssBaseline enableColorScheme />
      <AppAppBar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/people" element={<People />} />
        </Routes>
    </AppTheme>
  );
}
