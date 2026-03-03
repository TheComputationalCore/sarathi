export default function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-black text-white px-6">
      <div className="text-center space-y-6">
        <h1 className="text-6xl font-serif text-brand-gold">
          404
        </h1>
        <p className="text-gray-400">
          This path does not exist on the civilizational map.
        </p>
        <a
          href="/"
          className="inline-block px-6 py-3 bg-brand-gold text-black rounded-lg"
        >
          Return Home
        </a>
      </div>
    </div>
  );
}