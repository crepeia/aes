// Optional real API integration test. Requires an empty test account and a
// local server backed ONLY by a disposable database. Never uses project secrets.
const { test } = require('node:test');
const assert = require('node:assert/strict');
const { randomUUID } = require('node:crypto');

const base = process.env.AES_TEST_BASE_URL;
const token = process.env.AES_TEST_TOKEN;
const enabled = base && token && process.env.AES_TEST_DISPOSABLE_DB === '1';

test('real API: concurrent replay, points, conflict and daily replacement', { skip: !enabled }, async () => {
    assert.ok(['localhost', '127.0.0.1', '[::1]'].includes(new URL(base).hostname), 'Only a local disposable test server is allowed');
    const call = (path, options = {}) => fetch(`${base}/webresources/secured/${path}`, {
        ...options, signal: AbortSignal.timeout(15000),
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json', ...options.headers },
    });
    const recordResponse = await call('record/findByUser'); assert.equal(recordResponse.status, 200);
    const record = await recordResponse.json();
    const logsBefore = await call(`dailylog/find/${record.id}`);
    assert.ok(logsBefore.status === 404 || (logsBefore.status === 200 && (await logsBefore.json()).length === 0), 'Use an EMPTY test account');
    const challengesBefore = await call('challengeuser/findByUser');
    assert.equal(challengesBefore.status, 200);
    assert.equal((await challengesBefore.json()).filter(ch => ch.challengeId === 5 || ch.challengeId === 6).length, 0, 'Use an EMPTY test account');
    const today = new Date().toISOString().slice(0, 10);
    const key = randomUUID();
    const payload = { record: { id: record.id }, logDate: `${today}T00:00:00Z`, drinks: 0, context: '', consequences: '' };
    const save = (operationKey, body) => call('dailylog/editOrCreate', {
        method: 'POST', headers: { 'Idempotency-Key': operationKey, 'X-Dose-Challenges': 'true', 'X-Dose-Saved-On': today },
        body: JSON.stringify(body),
    });
    const replies = await Promise.all(Array.from({ length: 4 }, () => save(key, payload)));
    const results = [];
    for (const reply of replies) { assert.equal(reply.status, 200); results.push(await reply.text()); }
    assert.equal(new Set(results).size, 1);
    assert.ok(JSON.parse(results[0]).id);
    let logs = await (await call(`dailylog/find/${record.id}`)).json(); assert.equal(logs.length, 1);
    let challenges = (await (await call('challengeuser/findByUser')).json()).filter(ch => ch.challengeId === 5 || ch.challengeId === 6);
    assert.equal(challenges.length, 2); assert.equal(challenges.reduce((sum, ch) => sum + ch.score, 0), 13);
    assert.equal((await save(key, { ...payload, drinks: 3 })).status, 409);
    assert.equal((await save(randomUUID(), { ...payload, drinks: 3 })).status, 200);
    assert.equal((await save(key, payload)).status, 200);
    logs = await (await call(`dailylog/find/${record.id}`)).json();
    assert.equal(logs.length, 1); assert.equal(logs[0].drinks, 3);
    challenges = (await (await call('challengeuser/findByUser')).json()).filter(ch => ch.challengeId === 5 || ch.challengeId === 6);
    assert.equal(challenges.length, 1); assert.equal(challenges[0].score, 6);
});
