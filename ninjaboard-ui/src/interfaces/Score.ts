export interface Score {
    id: string;
    name: string;
    total: number;
    level: string;
    pointsToNextLevel: number;
    servicesSupportClosedIssues: number
    ["ServicesSupport Closed Issues"]: number,
}