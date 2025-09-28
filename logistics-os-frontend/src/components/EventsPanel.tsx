import React from "react";

type EventAction = { label: string; action: string };

export default function EventsPanel({ events, onAction }:
  { events: Array<{ id?: number; text: string; actions?: EventAction[] }>, onAction: (action: string) => void }) {
  return (
    <div className="w-80 bg-white border-l shadow p-4 flex flex-col gap-2">
      <h2 className="font-bold text-lg">События</h2>
      {events.map(e => (
        <div key={e.id ?? e.text} className="p-2 border rounded bg-gray-50">
          {e.text}
          {e.actions && (
            <div className="flex gap-2 mt-1">
              {e.actions.map((a, i) => (
                <button key={i} onClick={() => onAction(a.action)} className="text-blue-600 underline">
                  {a.label}
                </button>
              ))}
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
