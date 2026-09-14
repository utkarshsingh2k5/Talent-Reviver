import React, { useState, useEffect, useContext } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { usercontext } from '../appcontext';
import Styles from './outreachTracker.module.css';

function OutreachTracker() {
    const { jobId } = useParams();
    const navigate = useNavigate();
    const { backendURL } = useContext(usercontext);
    const [logs, setLogs] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    const fetchLogs = async () => {
        setIsLoading(true);
        try {
            const url = jobId === 'all'
                ? `${backendURL}/hr/outreach/all`
                : `${backendURL}/hr/jobs/${jobId}/outreach`;

            const response = await fetch(url, {
                credentials: 'include'
            });
            if (response.ok) {
                const data = await response.json();
                setLogs(data);
            }
        } catch (error) {
            console.error('Failed to fetch outreach logs', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchLogs();
    }, [jobId]);

    return (
        <div className={Styles.container}>
            <div className={Styles.header}>
                <button className={Styles.backBtn} onClick={() => navigate('/hr/dashboard')}>← Back to Dashboard</button>
                <div className={Styles.titleSection}>
                    <h1>Outreach Tracker</h1>
                    <p>{jobId === 'all' ? 'Monitoring all candidate responses' : `Monitoring candidate responses for Job ID: ${jobId}`}</p>
                </div>
            </div>

            {isLoading ? (
                <div className={Styles.loader}>Loading outreach logs...</div>
            ) : (
                <div className={Styles.mainContent}>
                    <table className={Styles.logTable}>
                        <thead>
                            <tr>
                                <th>Candidate Email</th>
                                <th>Sent Date</th>
                                <th>Delivery Status</th>
                                <th>Candidate Response</th>
                                <th>Response Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            {logs.length > 0 ? logs.map((log, index) => (
                                <tr key={index}>
                                    <td>{log.candidateEmail}</td>
                                    <td>{log.sentAt ? new Date(log.sentAt).toLocaleString() : 'Not yet sent'}</td>
                                    <td>
                                        <span className={`${Styles.statusBadge} ${Styles[log.deliveryStatus?.toLowerCase().replace('_', '')]}`}>
                                            {log.deliveryStatus}
                                        </span>
                                    </td>
                                    <td>
                                        <span className={`${Styles.resBadge} ${Styles[log.response?.toLowerCase().replace('_', '')]}`}>
                                            {log.response}
                                        </span>
                                    </td>
                                    <td>{log.responseAt ? new Date(log.responseAt).toLocaleString() : '-'}</td>
                                </tr>
                            )) : (
                                <tr>
                                    <td colSpan="5" className={Styles.noData}>No outreach recorded for this job yet.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default OutreachTracker;
