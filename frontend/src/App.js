import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import SanctionedNames from './sanctioned-names/SanctionedNames.js';

function App() {
  return (
    <Router>
      <SanctionedNames />
    </Router>
  );
}

export default App;
