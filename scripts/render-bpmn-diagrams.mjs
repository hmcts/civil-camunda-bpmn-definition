#!/usr/bin/env node
import { fileURLToPath } from 'node:url';
import path from 'node:path';
import fs from 'node:fs/promises';
import puppeteer from 'puppeteer';

const CHROME_FLAGS = [
  '--no-sandbox',
  '--disable-setuid-sandbox'
];

// Patch puppeteer launch to ensure the GitHub runner can start Chromium
const originalLaunch = puppeteer.launch.bind(puppeteer);
puppeteer.launch = function patchedLaunch(options = {}) {
  const existingArgs = Array.isArray(options.args) ? options.args : [];
  const args = [
    ...CHROME_FLAGS,
    ...existingArgs
  ];

  return originalLaunch({
    headless: options.headless ?? 'new',
    ...options,
    args
  });
};

const { convertAll } = await import('bpmn-to-image');

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(__dirname, '..');
const camundaDir = path.join(repoRoot, 'src', 'main', 'resources', 'camunda');
const outputDir = path.join(repoRoot, 'docs', 'bpmn-diagrams');

async function collectBpmnFiles(dir) {
  const files = [];

  async function walk(current) {
    const entries = await fs.readdir(current, { withFileTypes: true });
    for (const entry of entries) {
      const fullPath = path.join(current, entry.name);
      if (entry.isDirectory()) {
        await walk(fullPath);
      } else if (entry.isFile() && entry.name.endsWith('.bpmn')) {
        files.push(fullPath);
      }
    }
  }

  await walk(dir);
  return files.sort((a, b) => a.localeCompare(b));
}

async function main() {
  const filters = (process.env.BPMN_RENDER_ONLY || '')
    .split(',')
    .map((entry) => entry.trim())
    .filter(Boolean);

  let diagrams = await collectBpmnFiles(camundaDir);

  if (filters.length) {
    diagrams = diagrams.filter((diagram) =>
      filters.some((filter) => diagram.includes(filter))
    );
  }

  const limit = Number.parseInt(process.env.BPMN_RENDER_LIMIT || '', 10);
  if (!Number.isNaN(limit) && limit > 0) {
    diagrams = diagrams.slice(0, limit);
  }

  if (diagrams.length === 0) {
    console.log('No BPMN diagrams found');
    return;
  }

  await fs.mkdir(outputDir, { recursive: true });

  const conversions = diagrams.map((diagram) => {
    const filename = path.basename(diagram, '.bpmn');
    const outBase = path.join(outputDir, filename);
    return {
      input: diagram,
      outputs: [ `${outBase}.png` ]
    };
  });

  await convertAll(conversions, {
    minDimensions: { width: 400, height: 300 },
    footer: true,
    title: true,
    deviceScaleFactor: 1
  });
}

main().catch((error) => {
  console.error('Failed to render BPMN diagrams');
  console.error(error);
  process.exitCode = 1;
});
