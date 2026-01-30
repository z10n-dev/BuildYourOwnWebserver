import React from 'react';
import { useSSE } from '../hooks/useSSE';

// Formatting helper for uptime
const formatUptime = (miliseconds: number) => {
    const seconds = Math.floor(miliseconds / 1000); 
    const d = Math.floor(seconds / (3600 * 24));
    const h = Math.floor((seconds % (3600 * 24)) / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return `${d}d ${h}h ${m}m`;
};

export const Stats = () => {
    const { stats } = useSSE();

    if (!stats || stats.length === 0) return <div className="text-slate-500">Loading live telemetry...</div>;

    const statItems = [
        { label: 'CPU Usage', value: `${Math.floor(stats[stats.length - 1]?.cpuUsage * 100)}%`, color: stats[stats.length - 1]?.cpuUsage > 80 ? 'text-red-400' : 'text-emerald-400' },
        { label: 'Memory', value: `${Math.floor(stats[stats.length - 1]?.memoryUsage)} GiB`, color: 'text-blue-400' },
        { label: 'Active Conns', value: stats[stats.length - 1]?.activeConnections.toLocaleString(), color: 'text-purple-400' },
        { label: 'Total Requests', value: stats[stats.length - 1]?.totalRequests.toLocaleString(), color: 'text-amber-400' },
        { label: 'Uptime', value: formatUptime(stats[stats.length - 1]?.uptime), color: 'text-slate-300' },
    ];

    return (
        <div className="w-full p-6 bg-slate-900 rounded-xl shadow-lg border border-slate-800 mb-10">
            <div className="flex justify-between items-center mb-6">
                <h3 className="text-lg font-bold text-slate-100 uppercase tracking-wider">Live System Metrics</h3>
                <span className="flex h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4">
                {statItems.map((item, idx) => (
                    <div key={idx} className="p-4 bg-slate-800/50 rounded-lg border border-slate-700/50">
                        <p className="text-xs font-medium text-slate-500 uppercase mb-1">{item.label}</p>
                        <p className={`text-xl font-mono font-bold ${item.color}`}>
                            {item.value}
                        </p>
                        {/* Optional mini-progress bar for percentages */}
                        {item.label.includes('Usage') || item.label === 'Memory' ? (
                            <div className="w-full bg-slate-700 h-1 mt-2 rounded-full overflow-hidden">
                                <div 
                                    className={`h-full transition-all duration-500 ${item.color.replace('text', 'bg')}`}
                                    style={{ width: item.value }}
                                />
                            </div>
                        ) : null}
                    </div>
                ))}
            </div>
        </div>
    );
};