import * as React from 'react';
import {
  Page,
  PageHeader,
  Brand,
  Masthead,
  MastheadToggle,
  PageToggleButton,
  MastheadMain,
  MastheadBrand,
  MastheadContent,
  Nav,
  NavItem,
  NavList,
  PageSidebar,
  PageSection,
  Card
} from '@patternfly/react-core';
import BellIcon from '@patternfly/react-icons/dist/esm/icons/bell-icon';
import CogIcon from '@patternfly/react-icons/dist/esm/icons/cog-icon';
import HelpIcon from '@patternfly/react-icons/dist/esm/icons/help-icon';
import QuestionCircleIcon from '@patternfly/react-icons/dist/esm/icons/question-circle-icon';


interface IAppLayout {
    children: React.ReactNode;
}

interface NavOnSelectProps {
  groupId: number | string;
  itemId: number | string;
  to: string;
  event: React.FormEvent<HTMLInputElement>;
}

const AppLayout: React.FunctionComponent<IAppLayout> = ({ children }) => {

  const [activeItem, setActiveItem] = React.useState(0);

  const onNavSelect = (selectedItem: NavOnSelectProps) => {
    typeof selectedItem.itemId === 'number' && setActiveItem(selectedItem.itemId);
  };

/*   const pfIcon = "https://www.patternfly.org/v4/v4/images/pf-logo-small.5d35874fb79e43a65446b5ef48176722.svg";

  const logoProps = {
    href: '/',
    target: '_blank',
    src: 'https://www.patternfly.org/v4/images/logo__pf--reverse-on-md.92c3cd3e0181da4c832c5dd4de8513d6.svg'
  };
 */

  const masthead = (
    <Masthead>
      <MastheadToggle>
        <PageToggleButton variant="plain" aria-label="Global navigation">
        </PageToggleButton>
      </MastheadToggle>
      <MastheadMain>
        <MastheadBrand>
          <Brand
            widths={{ default: '100px', md: '100px', '2xl': '140px' }}
            src={process.env.PUBLIC_URL + '/logo.svg'}
            alt="Red Hat Community of Practice"
          >
            <source media="(min-width: 768px)" srcSet={process.env.PUBLIC_URL + '/logo.svg'} />
            <source srcSet={process.env.PUBLIC_URL + '/logo.svg'} />
          </Brand>
        </MastheadBrand>
      </MastheadMain>
    </Masthead>
  );


  const pageNav = (
    <Nav onSelect={onNavSelect}>
      <NavList>
        <NavItem itemId={0} isActive={activeItem === 0} to="#scorecard">
          Scorecards
        </NavItem>
        <NavItem itemId={1} isActive={activeItem === 1} to="#leaderboard">
          Leaderboard
        </NavItem>
        <NavItem itemId={2} isActive={activeItem === 2} to="#events">
          Events
        </NavItem>
        <NavItem itemId={3} isActive={activeItem === 3} to="#network">
          Tasks
        </NavItem>
        <NavItem itemId={4} isActive={activeItem === 4} to="#server">
          Registration Form
        </NavItem>
      </NavList>
    </Nav>
  );

  const sidebar = <PageSidebar nav={pageNav} />;


  const header = (
    <Masthead>
      <MastheadToggle>
        <PageToggleButton
          variant="plain"
          aria-label="Global navigation"
          id="centered-nav-toggle"
        >
        </PageToggleButton>
      </MastheadToggle>
      <MastheadMain>
        <MastheadBrand href="https://patternfly.org" target="_blank">
          <Brand alt='V' src={process.env.PUBLIC_URL + '/logo.svg'}> </Brand>
        </MastheadBrand>
      </MastheadMain>
      <MastheadContent></MastheadContent>
    </Masthead>
  );

    return (
      <Page
      header={masthead}
      sidebar={sidebar}
      >
        <PageSection isWidthLimited isCenterAligned>
          <Card>
           {children}
          </Card>
        </PageSection>
      </Page>
    );
}

export { AppLayout };