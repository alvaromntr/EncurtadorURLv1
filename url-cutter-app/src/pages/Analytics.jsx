import { useEffect, useMemo, useState } from 'react'

import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid
} from 'recharts'

import Navbar from '../components/Navbar'
import { useUrlStore } from '../store/useUrlStore'

export default function Analytics() {

  const {
    history,
    fetchUrls,
    fetchClicksByUrl,
    clicks
  } = useUrlStore()

  const [selectedUrlId, setSelectedUrlId] =
    useState(null)

  useEffect(() => {
    fetchUrls()
  }, [])

  useEffect(() => {

    if (selectedUrlId) {
      fetchClicksByUrl(selectedUrlId)
    }

  }, [selectedUrlId])

  const chartData = useMemo(() => {

  if (!Array.isArray(clicks)) {
    return []
  }

  const grouped = {}

  clicks.forEach(click => {

    const date =
      new Date(
        click.clickedAt ||
        click.createdAt
      ).toLocaleDateString('pt-BR')

    grouped[date] =
      (grouped[date] || 0) + 1
  })

  return Object.entries(grouped)
    .map(([date, total]) => ({
      date,
      total
    }))

}, [clicks])

  return (

    <div className="min-h-screen p-10 flex flex-col gap-6">

      <Navbar />

      <h1 className="text-3xl font-bold">
        📊 Analytics
      </h1>

      <select
        className="select select-bordered w-full max-w-md"
        value={selectedUrlId || ''}
        onChange={(e) =>
          setSelectedUrlId(
            Number(e.target.value)
          )
        }
      >

        <option value="">
          Selecione uma URL
        </option>

        {history.map(url => (

          <option
            key={url.id}
            value={url.id}
          >
            {url.originalUrl}
          </option>

        ))}

      </select>

      {selectedUrlId && (

        <div className="bg-base-200 p-6 rounded-xl">

          <h2 className="font-semibold mb-4">
            Evolução de Cliques
          </h2>

          <div className="h-96">

            <ResponsiveContainer
              width="100%"
              height="100%"
            >

              <LineChart data={chartData}>

                <CartesianGrid
                  strokeDasharray="3 3"
                />

                <XAxis dataKey="date" />

                <YAxis />

                <Tooltip />

                <Line
                  type="monotone"
                  dataKey="total"
                  stroke="#3B82F6"
                  strokeWidth={3}
                />

              </LineChart>

            </ResponsiveContainer>

          </div>

        </div>
      )}

    </div>
  )
}