"use client";

import { refresh } from 'next/cache';
import React, { useEffect, useState } from 'react'
import ToDoCard from '../components/ToDoCard';

export interface item {
    id: string;
    title: string;
    completed: boolean;
}

export default function page() {
    const [items, setItems] = useState<item[]>([]);
    const [refetch, setRefetch] = useState(false);
    const [newTodoTitle, setNewTodoTitle] = useState('');

    useEffect(() => {
        fetch('/api/todos')
            .then((res) => {
                return res.arrayBuffer().then((buffer) => {
                    const decoder = new TextDecoder();
                    const text = decoder.decode(buffer);
                    console.log("Fetched todos:", text);
                    setItems(JSON.parse(text));
                });
            })
    }, [refetch])

    const updateTodo = (item: item) => {
        fetch(`/api/todos/${item.id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ id: item.id, completed: item.completed, title: item.title }),
        }).then(() => {
            setRefetch(!refetch);
        });
    };

    const deleteTodo = (id: string) => {
        fetch(`/api/todos/${id}`, {
            method: 'DELETE',
        }).then(() => {
            setRefetch(!refetch);
        });
    };

    const addTodo = () => {
        if (newTodoTitle.trim()) {
            fetch('/api/todos', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ title: newTodoTitle, completed: false }),
            }).then(() => {
                setNewTodoTitle('');
                setRefetch(!refetch);
            });
        }
    };


  return (
    <div className='flex flex-col items-center mt-10 min-h-screen'>
        <h1 className='text-3xl font-bold mb-4'>Todo List</h1>
        <div className='w-full max-w-md mb-6'>
            <div className='flex gap-2'>
                <input
                    type='text'
                    value={newTodoTitle}
                    onChange={(e) => setNewTodoTitle(e.target.value)}
                    onKeyPress={(e) => {
                        if (e.key === 'Enter') {
                            addTodo();
                        }
                    }}
                    placeholder='Add a new todo...'
                    className='flex-1 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500'
                />
                <button
                    onClick={addTodo}
                    className='px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 font-medium'
                >
                    Add
                </button>
            </div>
        </div>
        <div className='space-y-2 w-full max-w-md'>
            {items.map((item) => (
                <ToDoCard onToggle={function (): void {
                    item.completed = !item.completed;
                    updateTodo(item);
                } } onDelete={function (id: string): void {
                    deleteTodo(id);
                } } key={item.id} {...item} id={item.id.toString()} 
                    onEdit={function (id: string, newTitle: string): void {
                        item.title = newTitle;
                        updateTodo(item);
                    } }
                />
            ))}
        </div>
    </div>
  )
}