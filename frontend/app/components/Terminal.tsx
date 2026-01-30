import React, { useEffect, useRef } from 'react';
import { logEntry } from '../hooks/useSSE';

interface TerminalProps {
  logs: logEntry[];
}

const Terminal: React.FC<TerminalProps> = ({ logs }) => {

  const terminalRef = useRef<HTMLDivElement>(null);
  // We use a ref to track if the user is at the bottom.
  // We default to true so it scrolls to bottom on initial load.
  const isAtBottomRef = useRef(true);

  // 1. Listen to scroll events to update our "isAtBottom" tracker
  const handleScroll = () => {
    if (!terminalRef.current) return;
    const { scrollTop, scrollHeight, clientHeight } = terminalRef.current;
    
    // Calculate distance from bottom
    const distanceFromBottom = scrollHeight - (scrollTop + clientHeight);
    
    // We use a small buffer (10px) to account for minor pixel discrepancies
    isAtBottomRef.current = distanceFromBottom < 10;
  };

  // 2. When logs change, scroll to bottom ONLY if we were already there
  useEffect(() => {
    if (isAtBottomRef.current && terminalRef.current) {
      terminalRef.current.scrollTop = terminalRef.current.scrollHeight;
    }
  }, [logs]);

  return (
    <div className="flex flex-col items-center justify-center">
      
      {/* Terminal Window */}
      <div className="w-full max-w-3xl rounded-lg shadow-2xl overflow-hidden border border-gray-700 bg-black">
        

        {/* Scrollable Content Area */}
        <div
          ref={terminalRef}
          onScroll={handleScroll}
          className="p-4 font-mono text-sm text-green-400 overflow-y-auto scroll-smooth"
          style={{ height: '70vh' }} // Fixed height of 70% of device height
        >
          {logs.map((log, index) => (
            <div key={index} className="mb-1">
              <span className="text-blue-400 mr-2">$</span>
              <span>[{log.logLevel}]</span> {log.timestamp.toLocaleString()} {log.message}
            </div>
          ))}
          
          {/* Blinking Cursor */}
          <div className="mt-2 animate-pulse">
            <span className="inline-block w-2 h-4 bg-green-400 align-middle"></span>
          </div>
        </div>
      </div>

      <p className="mt-4 text-gray-500 text-sm">
        Scroll up to pause auto-scrolling. Scroll back to bottom to resume.
      </p>
    </div>
  );
};

export default Terminal;