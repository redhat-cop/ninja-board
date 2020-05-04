//update the data here to make any content updates to the NavBar.
//top level objects, currently only 2, are for the expandable portion of the NavBar.
//the lower level objects under the "links" key are the actual links within that expandable.
export const navBarLinks = [
  {
    id: 1,
    expandableName: "Ninja Program",
    groupId: "ninja",
    links: [
      {
        id: 1,
        linkName: "Scorecards",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/scorecards.jsp"
      },
      {
        id: 2,
        linkName: "Leaderboard",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/leaderboard.jsp"
      },
      {
        id: 3,
        linkName: "Events",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/events.jsp"
      },
      {
        id: 4,
        linkName: "Tasks",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/tasks.jsp"
      },
      {
        id: 5,
        linkName: "Registration Form",
        target:
          "https://docs.google.com/a/redhat.com/forms/d/e/1FAIpQLSdWGcCks2zKKnVoZFQz3CieLQDc1lsSex_Knwh_-eyRm0ZQTg/viewform"
      },
      {
        id: 6,
        linkName: "Responses Spreadsheet",
        target:
          "https://docs.google.com/spreadsheets/d/1E91hT_ZpySyvhnANxqZ7hcBSM2EEd9TqfQF-cavB8hQ"
      },
      {
        id: 7,
        linkName: "Support - Trello Query",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/support-trello-card.jsp"
      },
      {
        id: 8,
        linkName: "Support - Trello Reconciliation",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/support-trello-all.jsp"
      }
    ]
  },
  {
    id: 2,
    expandableName: "Admin",
    groupId: "admin",
    links: [
      {
        id: 1,
        linkName: "Config (Here be dragons!)",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/config.jsp"
      },
      {
        id: 2,
        linkName: "Database (Here be dragons!)",
        target:
          "https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/database.jsp"
      }
    ]
  }
];
