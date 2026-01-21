import React, { useState } from 'react';

interface ToDoCardProps {
    id: string;
    title: string;
    completed: boolean;
    onToggle: (id: string) => void;
    onDelete: (id: string) => void;
    onEdit?: (id: string, newTitle: string) => void;
}

export default function ToDoCard({
    id,
    title,
    completed,
    onToggle,
    onDelete,
    onEdit,
}: ToDoCardProps) {
    const [isEditing, setIsEditing] = useState(false);
    const [editedTitle, setEditedTitle] = useState(title);

    const handleSaveEdit = () => {
        if (editedTitle.trim()) {
            onEdit?.(id, editedTitle);
            setIsEditing(false);
        }
    };

    const handleCancelEdit = () => {
        setEditedTitle(title);
        setIsEditing(false);
    };

    return (
        <div className="flex items-center gap-3 p-4 bg-white border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition-shadow">
            <input
                type="checkbox"
                checked={completed}
                onChange={() => onToggle(id)}
                className="w-5 h-5 cursor-pointer"
            />
            {isEditing ? (
                <input
                    type="text"
                    value={editedTitle}
                    onChange={(e) => setEditedTitle(e.target.value)}
                    className="flex-1 px-2 py-1 text-lg border border-blue-500 text-black rounded focus:outline-none focus:ring-2 focus:ring-blue-300"
                    autoFocus
                />
            ) : (
                <span
                    className={`flex-1 text-lg ${
                        completed ? 'line-through text-gray-400' : 'text-gray-900'
                    }`}
                >
                    {title}
                </span>
            )}
            {isEditing ? (
                <>
                    <button
                        onClick={handleSaveEdit}
                        className="px-3 py-1 text-sm bg-green-500 text-white rounded hover:bg-green-600 transition-colors"
                    >
                        Save
                    </button>
                    <button
                        onClick={handleCancelEdit}
                        className="px-3 py-1 text-sm bg-gray-500 text-white rounded hover:bg-gray-600 transition-colors"
                    >
                        Cancel
                    </button>
                </>
            ) : (
                <>
                    <button
                        onClick={() => setIsEditing(true)}
                        className="px-3 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
                    >
                        Edit
                    </button>
                    <button
                        onClick={() => onDelete(id)}
                        className="px-3 py-1 text-sm bg-red-500 text-white rounded hover:bg-red-600 transition-colors"
                    >
                        Delete
                    </button>
                </>
            )}
        </div>
    );
}