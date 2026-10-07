import { Link, Route, Routes } from 'react-router-dom';
import Temas from './views/Temas';
import Estudio from './views/Estudio';
import Repaso from './views/Repaso';
import Tests from './views/Tests';
import Material from './views/Material';
import './App.css';

export default function App() {
  return (
    <main className="container">
      <h1>Oposiciones IA (local)</h1>
      <nav>
        <ul>
          <li>
            <Link to="/temas">Temas</Link>
          </li>
          <li>
            <Link to="/repaso">Repaso</Link>
          </li>
          <li>
            <Link to="/tests">Tests</Link>
          </li>
          <li>
            <Link to="/material">Chuleta/Mapa</Link>
          </li>
        </ul>
      </nav>
      <Routes>
        <Route path="/" element={<Temas />} />
        <Route path="/temas" element={<Temas />} />
        <Route path="/temas/:id" element={<Estudio />} />
        <Route path="/repaso" element={<Repaso />} />
        <Route path="/tests" element={<Tests />} />
        <Route path="/material" element={<Material />} />
      </Routes>
    </main>
  );
}
