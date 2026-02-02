import React, { useEffect, useRef, useState } from 'react';
import { logEntry } from '../hooks/useSSE';

interface TerminalProps {
  logs: logEntry[];
}

const Terminal: React.FC<TerminalProps> = ({ logs }) => {
  const terminalRef = useRef<HTMLDivElement>(null);
  const isAtBottomRef = useRef(true);
  const [selectedLogLevel, setSelectedLogLevel] = useState<string | null>(null);

  const handleScroll = () => {
    if (!terminalRef.current) return;
    const { scrollTop, scrollHeight, clientHeight } = terminalRef.current;
    const distanceFromBottom = scrollHeight - (scrollTop + clientHeight);
    isAtBottomRef.current = distanceFromBottom < 10;
  };

  useEffect(() => {
    if (isAtBottomRef.current && terminalRef.current) {
      terminalRef.current.scrollTop = terminalRef.current.scrollHeight;
    }
  }, [logs, selectedLogLevel]);

  const filteredLogs = selectedLogLevel
    ? logs.filter((log) => log.logLevel === selectedLogLevel)
    : logs;

  const uniqueLogLevels = Array.from(new Set(logs.map((log) => log.logLevel)));

  return (
    <div className="flex flex-col items-center justify-center" style={{ height: '70vh' }}>
      <div className="w-full max-w-3xl rounded-lg shadow-2xl overflow-hidden border border-gray-700 bg-black">
        {/* Header with Log Level Dropdown */}
        <div className="flex justify-end items-center p-3 bg-gray-900 border-b border-gray-700">
          <label className="text-gray-400 text-sm mr-2">Filter:</label>
          <select
            value={selectedLogLevel || ''}
            onChange={(e) => setSelectedLogLevel(e.target.value || null)}
            className="px-3 py-1 text-sm bg-gray-800 text-gray-400 border border-gray-600 rounded cursor-pointer hover:border-gray-500 focus:outline-none focus:border-gray-500"
          >
            <option value="">All Levels</option>
            {uniqueLogLevels.map((level) => (
              <option key={level} value={level}>
                {level}
              </option>
            ))}
          </select>
        </div>

        {/* Scrollable Content Area */}
        <div
          ref={terminalRef}
          onScroll={handleScroll}
          className="p-4 font-mono text-sm text-green-400 overflow-y-auto scroll-smooth"
          style={{ height: '70vh' }}
        >
          {filteredLogs.map((log, index) => (
            <div key={index} className="mb-1">
              <span className="text-blue-400 mr-2">$</span>
              <span>[{log.logLevel}]</span> {log.timestamp.toLocaleString()} {log.message}
            </div>
          ))}
          <div className="mt-2 animate-pulse">
            <span className="inline-block w-2 h-4 bg-green-400 align-middle"></span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Terminal;
