import React, { use, useEffect, useState } from 'react'
import { useUrlStore } from '../store/useUrlStore'
import { useQrCodeStore } from '../store/useQrCodeStore'

export default function Shortener() {
  const [inputUrl, setInputUrl] = useState('')
  const [selectedUrl, setSelectedUrl] = useState(null)
  const { clickCounts, refreshClicks } = useUrlStore()

  const {
    shortenUrl,
    fetchUrls,
    deleteUrl,
    shortUrl,
    loading,
    error,
    history,
  } = useUrlStore()

  const {
    generateQrCode,
    qrCodeUrl,
    loading: qrLoading,
    clearQrCode,
  } = useQrCodeStore()

  // 🔥 Carrega histórico ao abrir
  useEffect(() => {
    fetchUrls()
  }, [])

  useEffect(() => {
    const interval = setInterval(() => {
      refreshClicks()
    }, 3000)

    return () => clearInterval(interval)
  }, [])

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

  const handleDelete = async (id) => {
    try {
      await deleteUrl(id)
    } catch (err) {
      console.error(err.message)
    }
  }

  const handleGenerateQr = async (shortUrl) => {
    try {
      setSelectedUrl(shortUrl)

      const shortCode = shortUrl.split('/').pop()
      await generateQrCode(shortCode)

      document.getElementById('qr_modal').showModal()
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
          onChange={(e) => setInputUrl(e.target.value)}
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
            href={shortUrl}
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
      <div className="w-full max-w-xl">
        <h2 className="font-semibold mb-2">Histórico</h2>

        {history.length === 0 && !loading && (
          <p className="text-sm opacity-60">
            Nenhuma URL criada ainda.
          </p>
        )}

        <ul className="flex flex-col gap-2">
          {history.map((item) => (
            <li
              key={item.id}
              className="bg-base-200 p-3 rounded-lg text-sm flex flex-col gap-2"
            >
              <p className="truncate">
                <strong>Original:</strong> {item.originalUrl}
              </p>

              <a
                href={item.shortUrl}
                target="_blank"
                rel="noreferrer"
                onClick={() => {
                  setTimeout(() => {
                    refreshClicks()
                    item.clickCount = (item.clickCount ?? 0) + 1
                  }, 500) // 300–1000ms funciona bem
                }}
                className="link link-primary break-all"
              >
                {item.shortUrl}
              </a>

              <div className="flex justify-between items-center mt-2 gap-2">
                <span className="text-xs opacity-60">
                  Cliques: {clickCounts[item.id] ?? 0}
                </span>

                <div className="flex gap-2">
                  <button
                    className="btn btn-xs btn-secondary"
                    onClick={() => handleGenerateQr(item.shortUrl)}
                  >
                    QR Code
                  </button>

                  <button
                    className="btn btn-xs btn-error"
                    onClick={() => handleDelete(item.id)}
                    disabled={loading}
                  >
                    Deletar
                  </button>
                </div>
              </div>
            </li>
          ))}
        </ul>
      </div>

      {/* 🔥 MODAL QR CODE */}
      <dialog id="qr_modal" className="modal">
        <div className="modal-box max-w-md flex flex-col items-center gap-4">
          <h3 className="font-bold text-lg">QR Code</h3>

          {selectedUrl && (
            <p className="text-xs opacity-60 break-all text-center">
              {selectedUrl}
            </p>
          )}

          {qrLoading && (
            <span className="loading loading-spinner loading-lg"></span>
          )}

          {qrCodeUrl && !qrLoading && (
            <>
              <img
                src={qrCodeUrl}
                alt="QR Code"
                className="w-52 h-52"
              />

              <a
                href={qrCodeUrl}
                download="qrcode.png"
                className="btn btn-primary btn-sm"
              >
                Baixar
              </a>
            </>
          )}

          <div className="modal-action">
            <form
              method="dialog"
              onSubmit={() => {
                setSelectedUrl(null)
                clearQrCode()
              }}
            >
              <button className="btn">Fechar</button>
            </form>
          </div>
        </div>
      </dialog>
    </div>
  )
}