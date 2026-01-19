import React from 'react';

interface ButtonProps {
    href: string;
    label: string;
    target?: '_blank' | '_self';
}

const Button: React.FC<ButtonProps> = ({ href, label, target = '_self' }) => {
    return (
        <a
            href={href}
            target={target}
            rel={target === '_blank' ? 'noopener noreferrer' : undefined}
            className="px-5 py-3 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors inline-block text-xl"
        >
            {label}
        </a>
    );
};

export default Button;