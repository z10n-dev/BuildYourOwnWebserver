import type {ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';

import styles from './index.module.css';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero', styles.heroBanner)}>
      <div className="container">
        <Heading as="h1" className={styles.heroTitle}>
          Glass Box
        </Heading>
        <p className={styles.heroSubtitle}>
          An educational HTTP/1.1 web server built from scratch in Java
        </p>
        <p className={styles.heroDescription}>
          Demystify web protocols by watching your HTTP requests flow through
          parsing, routing, and response pipelines in real time.
        </p>
        <div className={styles.buttons}>
          <Link
            className="button button--primary button--lg"
            to="/docs/intro">
            Get Started
          </Link>
          <Link
            className="button button--outline button--lg"
            to="https://github.com/z10n-dev/BuildYourOwnWebserver">
            GitHub
          </Link>
        </div>
      </div>
    </header>
  );
}

type FeatureItem = {
  icon: string;
  title: string;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
  {
    icon: '{}',
    title: 'Raw HTTP/1.1 from Scratch',
    description: (
      <>
        No frameworks, no magic. Built directly on TCP sockets with a custom
        request parser, router, and response builder so you can see exactly how
        HTTP works under the hood.
      </>
    ),
  },
  {
    icon: '\u2261',
    title: 'Virtual Threads (Java 25)',
    description: (
      <>
        Leverages Project Loom virtual threads for lightweight, high-concurrency
        request handling without the complexity of traditional thread pools.
      </>
    ),
  },
  {
    icon: '\u21C4',
    title: 'Real-time SSE Streaming',
    description: (
      <>
        Server-Sent Events push live metrics, logs, and connection stats to a
        client-side dashboard as requests flow through the server.
      </>
    ),
  },
  {
    icon: '\u2302',
    title: 'Virtual Host Support',
    description: (
      <>
        Serve multiple domains from a single server instance, each with its own
        document root, routes, and handler configuration.
      </>
    ),
  },
  {
    icon: '/api',
    title: 'Built-in REST API',
    description: (
      <>
        Includes a fully functional ToDo API with CRUD operations, demonstrating
        route-based handlers, JSON serialization, and proper HTTP methods.
      </>
    ),
  },
  {
    icon: '\u2699',
    title: 'YAML Configuration',
    description: (
      <>
        Declarative YAML config for ports, log levels, virtual hosts, and
        route-to-handler mappings. Supports dev and production profiles.
      </>
    ),
  },
];

function Feature({icon, title, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className={styles.featureCard}>
        <div className={styles.featureIcon}>{icon}</div>
        <Heading as="h3" className={styles.featureTitle}>{title}</Heading>
        <p className={styles.featureDescription}>{description}</p>
      </div>
    </div>
  );
}

function FeaturesSection() {
  return (
    <section className={styles.features}>
      <div className="container">
        <Heading as="h2" className={styles.sectionTitle}>
          Core Features
        </Heading>
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}

function ArchitectureSection() {
  return (
    <section className={styles.architecture}>
      <div className="container">
        <Heading as="h2" className={styles.sectionTitle}>
          Request Flow
        </Heading>
        <p className={styles.sectionSubtitle}>
          Every request passes through a transparent pipeline you can inspect and learn from
        </p>
        <div className={styles.flowContainer}>
          {[
            {label: 'TCP Connection', detail: 'Client connects via raw socket'},
            {label: 'HTTP Parsing', detail: 'Headers and body extracted from byte stream'},
            {label: 'Host Resolution', detail: 'Virtual host matched by Host header'},
            {label: 'Route Matching', detail: 'URL pattern mapped to handler'},
            {label: 'Handler Execution', detail: 'Request processed, response built'},
            {label: 'Response & Log', detail: 'Response sent, metrics streamed via SSE'},
          ].map((step, idx) => (
            <div key={idx} className={styles.flowStep}>
              <div className={styles.flowStepNumber}>{idx + 1}</div>
              <div className={styles.flowStepContent}>
                <strong>{step.label}</strong>
                <span>{step.detail}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function TechStackSection() {
  return (
    <section className={styles.techStack}>
      <div className="container">
        <Heading as="h2" className={styles.sectionTitle}>
          Tech Stack
        </Heading>
        <div className={styles.techGrid}>
          {[
            {name: 'Java 25', detail: 'Language & Runtime'},
            {name: 'Virtual Threads', detail: 'Concurrency Model'},
            {name: 'Jackson', detail: 'JSON & YAML Processing'},
            {name: 'Maven', detail: 'Build System'},
            {name: 'Raw TCP Sockets', detail: 'Networking Layer'},
            {name: 'Reflections', detail: 'Handler Discovery'},
          ].map((tech, idx) => (
            <div key={idx} className={styles.techItem}>
              <strong>{tech.name}</strong>
              <span>{tech.detail}</span>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

export default function Home(): ReactNode {
  return (
    <Layout
      title="Glass Box - Educational HTTP Server"
      description="Glass Box is an educational HTTP/1.1 server built from scratch in Java. Watch your requests flow through parsing, routing, and response pipelines in real time.">
      <HomepageHeader />
      <main>
        <FeaturesSection />
        <ArchitectureSection />
        <TechStackSection />
      </main>
    </Layout>
  );
}
