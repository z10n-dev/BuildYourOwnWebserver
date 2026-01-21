"use client";

import { refresh } from 'next/cache';
import React, { useEffect, useState } from 'react'
import ToDoCard from '../components/ToDoCard';

export default function page() {
    const [items, setItems] = useState<{ id: number; title: string; completed: boolean }[]>([]);
    const [refetch, setRefetch] = useState(false);

    useEffect(() => {
        fetch('http://localhost:8080/api/todos')
            .then((res) => {
                return res.arrayBuffer().then((buffer) => {
                    const decoder = new TextDecoder();
                    const text = decoder.decode(buffer);
                    console.log("Fetched todos:", text);
                    setItems(JSON.parse(text));
                });
            })
    }, [refetch])


  return (
    <div className='flex flex-col items-center mt-10 min-h-screen'>
        <h1 className='text-3xl font-bold mb-4'>Todo List</h1>
        <div className='space-y-2 w-full max-w-md'>
            {items.map((item) => (
                <ToDoCard onToggle={function (): void {
                    throw new Error('Function not implemented.');
                } } onDelete={function (id: string): void {
                    throw new Error('Function not implemented.');
                } } key={item.id} {...item} id={item.id.toString()} />
            ))}
        </div>
    </div>
  )
}
