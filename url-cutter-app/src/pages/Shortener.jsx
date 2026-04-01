import React, { useState } from 'react'
import { useUrlStore } from '../store/useUrlStore'

export default function Shortener() {
  const [inputUrl, setInputUrl] = useState('')

  const {
    shortenUrl,
    shortUrl,
    loading,
    error,
    history,
  } = useUrlStore()

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (!inputUrl) return

    try {
      await shortenUrl(inputUrl)
      setInputUrl('')
    } catch (err) {
      console.error(err.message)
    }
  }

  return (
    <div className="flex flex-col items-center justify-start p-10 gap-6 w-full">
      <h1 className="text-3xl font-bold">🔗 Encurtador de URL</h1>

      {/* Form */}
      <form onSubmit={handleSubmit} className="flex gap-2 w-full max-w-xl">
        <input
          type="text"
          placeholder="Cole sua URL aqui..."
          className="input input-bordered w-full"
          value={inputUrl}
          onChange={(e) => {
            console.log('Input URL:', e.target.value)
            setInputUrl(e.target.value)}}
        />

        <button
          type="submit"
          className="btn btn-primary"
          disabled={loading}
        >
          {loading ? 'Encurtando...' : 'Encurtar'}
        </button>
      </form>

      {/* Resultado */}
      {shortUrl && (
        <div className="bg-base-200 p-4 rounded-xl w-full max-w-xl">
          <p className="text-sm opacity-70">URL encurtada:</p>

          <a
            href={`${shortUrl}`}
            target="_blank"
            rel="noreferrer"
            className="link link-primary break-all"
          >
            {shortUrl}
          </a>
        </div>
      )}

      {/* Erro */}
      {error && (
        <div className="alert alert-error w-full max-w-xl">
          <span>{error}</span>
        </div>
      )}

      {/* Histórico */}
      {history.length > 0 && (
        <div className="w-full max-w-xl">
          <h2 className="font-semibold mb-2">Histórico</h2>

          <ul className="flex flex-col gap-2">
            {history.map((item, index) => (
              <li
                key={index}
                className="bg-base-200 p-3 rounded-lg text-sm"
              >
                <p className="truncate">
                  <strong>Original:</strong> {item.originalUrl}
                </p>

                <a
                  href={`http://localhost:8081/api/${item.shortUrl}`}
                  target="_blank"
                  rel="noreferrer"
                  className="link link-primary"
                >
                  {item.shortUrl}
                </a>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}