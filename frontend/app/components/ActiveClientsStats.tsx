"use client";

import React, { useState, useEffect } from 'react';
import useSWR from 'swr';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  AreaChart,
  Area
} from 'recharts';

const fetcher = (url: string) => fetch(url).then((res) => res.json());

export default function ActiveClientsGraph() {
  const [dataHistory, setDataHistory] = useState<{ time: string; count: number }[]>([]);

  // SWR handles the polling logic automatically
  const { data, error } = useSWR('/api/active-clients', fetcher, {
    refreshInterval: 1000, // 1 second
  });

  useEffect(() => {
    if (data?.count !== undefined) {
      const now = new Date();
      const timeLabel = `${now.getHours()}:${now.getMinutes()}:${now.getSeconds()}`;
      
      setDataHistory((prev) => {
        const newData = [...prev, { time: timeLabel, count: data.count }];
        // Keep only the last 20 data points for a clean sliding window
        return newData.slice(-20);
      });
    }
  }, [data]);

  if (error) return <div className="p-4 text-red-500">Failed to load data</div>;

  return (
    <div className="w-full p-6 bg-slate-900 rounded-xl shadow-sm border border-slate-800" style={{ height: '70vh' }}>
      <div className="flex justify-between items-center">
        <h3 className="text-lg font-semibold text-slate-300">Live Active Clients</h3>
        <span className="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm font-medium">
          {data?.count ?? 0} Online
        </span>
      </div>

      <div className='my-4' style={{ width: '100%', height: '85%' }}>
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart data={dataHistory}>
          <defs>
            <linearGradient id="colorCount" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3}/>
              <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
          <XAxis 
            dataKey="time" 
            hide 
          />
          <YAxis 
            stroke="#94a3b8" 
            fontSize={12} 
            tickLine={false} 
            axisLine={false} 
          />
          <Tooltip />
          <Area
            type="monotone"
            dataKey="count"
            stroke="#3b82f6"
            strokeWidth={2}
            fillOpacity={1}
            fill="url(#colorCount)"
            isAnimationActive={true}
            animationDuration={300}
          />
        </AreaChart>
      </ResponsiveContainer>
        </div>
    </div>
  );
}