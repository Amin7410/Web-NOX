import { Button } from '@nox/ui'

function App() {
    return (
        <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
            <h1>NOX Studio</h1>
            <p>This is a Vite + React application using a shared UI library.</p>
            <div style={{ marginTop: '20px' }}>
                <Button onClick={() => alert('Clicked!')}>Click me (From @nox/ui)</Button>
            </div>
        </div>
    )
}

export default App
