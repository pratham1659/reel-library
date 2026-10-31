//
//  SettingsView.swift
//  ReelLibrary
//

import SwiftUI
import UIKit

public struct SettingsView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel = SettingsViewModel()
    @State private var isShowingKeyVisible: Bool = false
    @State private var isShowingImportPrompt: Bool = false
    @State private var importJsonInput: String = ""

    public init() {}

    public var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    // 1. Engine Mode Selection
                    engineModeSection

                    // 2. Groq Cloud API Key Management
                    apiKeySection

                    // 3. Real-Time Cloud Telemetry Dashboard
                    telemetrySection

                    // 4. Daily Usage Visualization
                    usageGraphSection

                    // 5. Data Vault Management (Import & Export)
                    dataManagementSection

                    // 6. About & Hardware Capabilities
                    aboutSection

                    Spacer(minLength: 30)
                }
                .padding(20)
            }
            .background(Color.obsidianBg.ignoresSafeArea())
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") {
                        viewModel.saveGroqApiKey()
                        dismiss()
                    }
                    .font(.appBodyLead)
                    .fontWeight(.semibold)
                    .foregroundStyle(Color.activeBlue)
                }
            }
            .alert("Import Vault JSON", isPresented: $isShowingImportPrompt) {
                TextField("Paste ReelLibraryExport JSON...", text: $importJsonInput)
                    .textInputAutocapitalization(.never)
                Button("Import") {
                    _ = viewModel.importVault(jsonString: importJsonInput)
                    importJsonInput = ""
                }
                Button("Paste Clipboard") {
                    if let str = UIPasteboard.general.string {
                        _ = viewModel.importVault(jsonString: str)
                    }
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text("Paste exported Reel Library JSON to restore or migrate your indexed reels.")
            }
        }
    }

    // MARK: - 1. Engine Mode
    private var engineModeSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader(title: "PROCESSING ENGINE", icon: "cpu")

            VStack(spacing: 10) {
                ForEach(EngineMode.allCases, id: \.self) { mode in
                    Button {
                        viewModel.engineMode = mode
                    } label: {
                        HStack(alignment: .top, spacing: 12) {
                            ZStack {
                                Circle()
                                    .fill(viewModel.engineMode == mode ? Color.activeBlue : Color.obsidianPill)
                                    .frame(width: 20, height: 20)
                                    .overlay(
                                        Circle()
                                            .stroke(viewModel.engineMode == mode ? Color.white : Color.obsidianCardBorder, lineWidth: 1.5)
                                    )
                                if viewModel.engineMode == mode {
                                    Circle()
                                        .fill(Color.white)
                                        .frame(width: 8, height: 8)
                                }
                            }
                            .padding(.top, 2)

                            VStack(alignment: .leading, spacing: 3) {
                                Text(mode.displayName)
                                    .font(.appBodyLead)
                                    .fontWeight(.semibold)
                                    .foregroundStyle(Color.textPrimary)

                                Text(mode.modeDescription)
                                    .font(.appCaptionSmall)
                                    .foregroundStyle(Color.textSecondary)
                                    .lineSpacing(2)
                            }

                            Spacer()
                        }
                        .padding(14)
                        .background(
                            RoundedRectangle(cornerRadius: 14)
                                .fill(viewModel.engineMode == mode ? Color.activeBlue.opacity(0.12) : Color.obsidianCard)
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(viewModel.engineMode == mode ? Color.activeBlue : Color.obsidianCardBorder, lineWidth: 1)
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }

    // MARK: - 2. Groq Cloud API Key
    private var apiKeySection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader(title: "GROQ API KEY", icon: "key.fill")

            VStack(alignment: .leading, spacing: 10) {
                HStack(spacing: 8) {
                    Group {
                        if isShowingKeyVisible {
                            TextField("Enter Groq API Key (gsk_...)", text: $viewModel.groqApiKey)
                        } else {
                            SecureField("Enter Groq API Key (gsk_...)", text: $viewModel.groqApiKey)
                        }
                    }
                    .font(.appBodyRegular)
                    .foregroundStyle(Color.textPrimary)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()

                    Button {
                        isShowingKeyVisible.toggle()
                    } label: {
                        Image(systemName: isShowingKeyVisible ? "eye.slash" : "eye")
                            .font(.system(size: 14))
                            .foregroundStyle(Color.textMuted)
                    }

                    Button {
                        if let clip = UIPasteboard.general.string {
                            viewModel.groqApiKey = clip
                        }
                    } label: {
                        Text("Paste")
                            .font(.appCaptionSmall)
                            .foregroundStyle(Color.cyanAccent)
                    }
                }
                .padding(12)
                .background(RoundedRectangle(cornerRadius: 12).fill(Color.obsidianPill))
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.obsidianPillBorder, lineWidth: 1))

                HStack {
                    // Test Connection Button
                    Button {
                        viewModel.testConnection()
                    } label: {
                        HStack(spacing: 6) {
                            if viewModel.isTestingConnection {
                                ProgressView()
                                    .tint(.white)
                                    .scaleEffect(0.8)
                            } else {
                                Image(systemName: "bolt.horizontal.fill")
                            }
                            Text("Test Connection")
                        }
                        .font(.appCaptionPill)
                        .foregroundStyle(.white)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(Color.activeBlue)
                        .clipShape(Capsule())
                    }
                    .buttonStyle(.plain)
                    .disabled(viewModel.isTestingConnection)

                    Spacer()

                    // Status Result
                    if let result = viewModel.connectionTestResult {
                        Text(result)
                            .font(.appCaptionSmall)
                            .foregroundStyle(viewModel.isConnectionSuccess ? Color.statusReady : Color.statusError)
                    }
                }

                Text("Your API key is saved directly to Apple's Secure Enclave Keychain and is never synced to external servers.")
                    .font(.appCaptionSmall)
                    .foregroundStyle(Color.textMuted)
            }
            .padding(16)
            .neumorphicCard(cornerRadius: 16)
        }
    }

    // MARK: - 3. Real-Time Cloud Telemetry
    private var telemetrySection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                sectionHeader(title: "CLOUD TELEMETRY DASHBOARD", icon: "gauge.with.needle")
                Spacer()
                Button("Reset") {
                    viewModel.resetTelemetry()
                }
                .font(.appCaptionSmall)
                .foregroundStyle(Color.statusError)
            }

            VStack(spacing: 12) {
                // Token Metrics Row
                HStack(spacing: 10) {
                    metricCard(title: "Total Tokens", value: "\(viewModel.totalTokensUsed)", color: Color.sparkleOrange)
                    metricCard(title: "Prompt", value: "\(viewModel.totalPromptTokens)", color: Color.cyanAccent)
                    metricCard(title: "Completion", value: "\(viewModel.totalCompletionTokens)", color: Color.activeBlue)
                }

                // Diagnostics Row
                HStack {
                    VStack(alignment: .leading, spacing: 3) {
                        Text("LAST CALL LATENCY")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundStyle(Color.textMuted)
                        Text("\(viewModel.lastCallLatencyMs) ms")
                            .font(.appMonoStat)
                            .foregroundStyle(Color.statusReady)
                    }

                    Spacer()

                    VStack(alignment: .trailing, spacing: 3) {
                        Text("STATUS & PROVIDER")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundStyle(Color.textMuted)
                        Text("\(viewModel.lastCallStatusCode) • \(viewModel.lastCallProvider)")
                            .font(.appCaptionSmall)
                            .foregroundStyle(Color.textSecondary)
                    }
                }
                .padding(12)
                .background(RoundedRectangle(cornerRadius: 12).fill(Color.obsidianPill))
            }
            .padding(16)
            .neumorphicCard(cornerRadius: 16)
        }
    }

    // MARK: - 4. Usage Graph
    private var usageGraphSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader(title: "DAILY TOKEN CONSUMPTION", icon: "chart.bar.fill")

            VStack(alignment: .leading, spacing: 12) {
                if viewModel.dailyUsage.isEmpty {
                    Text("No usage recorded yet.")
                        .font(.appCaptionSmall)
                        .foregroundStyle(Color.textMuted)
                        .padding(.vertical, 20)
                } else {
                    let maxTokens = max(viewModel.dailyUsage.map { $0.totalTokens }.max() ?? 1, 1)

                    HStack(alignment: .bottom, spacing: 12) {
                        ForEach(viewModel.dailyUsage) { day in
                            VStack(spacing: 6) {
                                Text("\(day.totalTokens / 1000)k")
                                    .font(.system(size: 9, weight: .semibold, design: .monospaced))
                                    .foregroundStyle(Color.cyanAccent)

                                // Bar
                                RoundedRectangle(cornerRadius: 6)
                                    .fill(
                                        LinearGradient(
                                            colors: [Color.cyanAccent, Color.activeBlue],
                                            startPoint: .top,
                                            endPoint: .bottom
                                        )
                                    )
                                    .frame(height: max(CGFloat(day.totalTokens) / CGFloat(maxTokens) * 90, 8))

                                // Date Label
                                Text(day.period.suffix(5)) // "10-31"
                                    .font(.system(size: 10, weight: .medium))
                                    .foregroundStyle(Color.textMuted)
                            }
                            .frame(maxWidth: .infinity)
                        }
                    }
                    .frame(height: 140)
                    .padding(.top, 10)
                }
            }
            .padding(16)
            .neumorphicCard(cornerRadius: 16)
        }
    }

    // MARK: - 5. Data Vault Management
    private var dataManagementSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader(title: "DATA VAULT", icon: "externaldrive.fill")

            HStack(spacing: 12) {
                // Export Button
                Button {
                    if let json = viewModel.exportVault() {
                        UIPasteboard.general.string = json
                        viewModel.importStatusMessage = "Copied entire vault JSON to clipboard!"
                    }
                } label: {
                    HStack(spacing: 6) {
                        Image(systemName: "square.and.arrow.up")
                        Text("Export JSON")
                    }
                    .font(.appBodyRegular)
                    .fontWeight(.semibold)
                    .foregroundStyle(Color.textPrimary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.obsidianPill)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.obsidianPillBorder, lineWidth: 1))
                }
                .buttonStyle(.plain)

                // Import Button
                Button {
                    isShowingImportPrompt = true
                } label: {
                    HStack(spacing: 6) {
                        Image(systemName: "square.and.arrow.down")
                        Text("Import JSON")
                    }
                    .font(.appBodyRegular)
                    .fontWeight(.semibold)
                    .foregroundStyle(Color.sparkleOrange)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.obsidianPill)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.sparkleOrange.opacity(0.3), lineWidth: 1))
                }
                .buttonStyle(.plain)
            }

            if let msg = viewModel.importStatusMessage {
                Text(msg)
                    .font(.appCaptionSmall)
                    .foregroundStyle(Color.statusReady)
            }
        }
    }

    // MARK: - 6. About
    private var aboutSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            sectionHeader(title: "ABOUT REEL LIBRARY", icon: "info.circle")

            VStack(alignment: .leading, spacing: 6) {
                HStack {
                    Text("Version")
                        .font(.appBodyRegular)
                        .foregroundStyle(Color.textSecondary)
                    Spacer()
                    Text("1.0.0 (Build 1)")
                        .font(.appMonoSmall)
                        .foregroundStyle(Color.textPrimary)
                }

                HStack {
                    Text("Architecture")
                        .font(.appBodyRegular)
                        .foregroundStyle(Color.textSecondary)
                    Spacer()
                    Text("Swift 6 • iOS 17+ • SQLite FTS5")
                        .font(.appCaptionSmall)
                        .foregroundStyle(Color.cyanAccent)
                }

                HStack {
                    Text("Hardware Engine")
                        .font(.appBodyRegular)
                        .foregroundStyle(Color.textSecondary)
                    Spacer()
                    Text("Apple Neural Engine Active")
                        .font(.appCaptionSmall)
                        .foregroundStyle(Color.statusReady)
                }
            }
            .padding(14)
            .background(RoundedRectangle(cornerRadius: 12).fill(Color.obsidianCard))
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.obsidianCardBorder, lineWidth: 1))
        }
    }

    // MARK: - Helpers
    private func sectionHeader(title: String, icon: String) -> some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 11, weight: .bold))
                .foregroundStyle(Color.sparkleOrange)
            Text(title)
                .font(.appCaptionSmall)
                .fontWeight(.bold)
                .foregroundStyle(Color.textMuted)
        }
    }

    private func metricCard(title: String, value: String, color: Color) -> some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.appMonoStat)
                .fontWeight(.bold)
                .foregroundStyle(color)
            Text(title)
                .font(.system(size: 10, weight: .medium))
                .foregroundStyle(Color.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(RoundedRectangle(cornerRadius: 10).fill(Color.obsidianPill))
    }
}
