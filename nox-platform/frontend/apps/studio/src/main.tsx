import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import { StudioProvider } from './context/StudioContext'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <StudioProvider>
            <App />
        </StudioProvider>
    </React.StrictMode>,
)
