"use client";

import React, { useState, useEffect } from 'react';
import {
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  AreaChart,
  Area
} from 'recharts';
import { useSSE } from '../hooks/useSSE';

export default function ActiveClientsGraph() {
  const [dataHistory, setDataHistory] = useState<{ time: string; count: number }[]>([]);
  
  // 1. Grab the live count from your global SSE hook
  const { activeCount } = useSSE();

  // 2. Whenever activeCount changes, push a new point to the graph
  useEffect(() => {
    const now = new Date();
    const timeLabel = `${now.getHours()}:${now.getMinutes()}:${now.getSeconds()}`;
    
    setDataHistory((prev) => {
      const newData = [...prev, { time: timeLabel, count: activeCount }];
      // Keep only the last 20 data points
      return newData.slice(-20);
    });
  }, [activeCount]); // This effect triggers every time the server sends a new count

  return (
    <div className="w-full p-6 bg-slate-900 rounded-xl shadow-sm border border-slate-800" style={{ height: '70vh' }}>
      <div className="flex justify-between items-center">
        <h3 className="text-lg font-semibold text-slate-300">Live Active Clients (SSE)</h3>
        <span className="px-3 py-1 bg-blue-500/20 text-blue-400 border border-blue-500/30 rounded-full text-sm font-medium">
          {activeCount} Online
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
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#1e293b" />
            <XAxis dataKey="time" hide />
            <YAxis 
              stroke="#94a3b8" 
              fontSize={12} 
              tickLine={false} 
              axisLine={false} 
            />
            <Tooltip 
              contentStyle={{ backgroundColor: '#0f172a', borderColor: '#1e293b' }}
              itemStyle={{ color: '#3b82f6' }}
            />
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