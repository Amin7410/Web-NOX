export default function Home() {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50">
      <div className="text-center">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">Welcome to NOX Portal</h1>
        <p className="text-xl text-gray-600 mb-8">Manage your projects and collaborate with your team</p>
        <a 
          href="/projects"
          className="inline-flex items-center px-6 py-3 bg-[#4F46E5] hover:bg-[#4338CA] text-white rounded-lg font-medium transition-colors"
        >
          Go to Projects
        </a>
      </div>
    </div>
  );
}
