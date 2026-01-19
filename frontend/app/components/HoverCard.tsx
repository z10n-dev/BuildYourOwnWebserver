import React, { ReactNode } from 'react';

interface HoverCardProps {
    children: ReactNode;
    className?: string;
}

export const HoverCard: React.FC<HoverCardProps> = ({ children, className = '' }) => {
    return (
        <div
            className={`
                bg-slate-800 rounded-lg shadow-md
                transition-all duration-200 ease-in-out
                 hover:-translate-y-1
                p-6 cursor-pointer border-2 border-transparent hover:border-blue-500
                ${className}
            `}
        >
            {children}
        </div>
    );
};