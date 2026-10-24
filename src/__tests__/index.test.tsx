import * as Moxo from '../index';

jest.mock('../NativeMoxoModule', () => {
  const subscription = { remove: jest.fn() };
  return {
    __esModule: true,
    default: {
      setup: jest.fn(),
      link: jest.fn(() => Promise.resolve(true)),
      unlink: jest.fn(),
      showMeetRinger: jest.fn(() =>
        Promise.resolve('Show meet ringer success')
      ),
      enableTapWindowCloseEvent: jest.fn(),
      onUnreadMessageCountUpdated: jest.fn(() => subscription),
      onTapWindowClose: jest.fn(() => subscription),
    },
  };
});

const mockNative = jest.requireMock('../NativeMoxoModule').default;
const mockSubscription = mockNative.onTapWindowClose();

beforeEach(() => jest.clearAllMocks());

it('passes setup options through as an object', () => {
  Moxo.setup('example.com', { ignoreBadCert: false });
  expect(mockNative.setup).toHaveBeenCalledWith('example.com', {
    ignoreBadCert: false,
  });
});

it('accepts setup options given as a JSON string', () => {
  Moxo.setup('example.com', '{"certOrgName":"Moxo"}' as unknown as object);
  expect(mockNative.setup).toHaveBeenCalledWith('example.com', {
    certOrgName: 'Moxo',
  });
});

it('defaults unlink to not keeping notifications', () => {
  Moxo.unlink();
  expect(mockNative.unlink).toHaveBeenCalledWith(false);
});

it('returns the native promise', async () => {
  await expect(Moxo.link('token')).resolves.toBe(true);
  await expect(Moxo.showMeetRinger('1')).resolves.toBe(
    'Show meet ringer success'
  );
});

it('returns removable subscriptions for events', () => {
  const callback = jest.fn();
  expect(Moxo.onUnreadMessageCountUpdated(callback)).toBe(mockSubscription);
  expect(mockNative.onUnreadMessageCountUpdated).toHaveBeenCalledWith(callback);
  expect(mockNative.enableTapWindowCloseEvent).not.toHaveBeenCalled();
});

it('enables the close event only when subscribed', () => {
  const callback = jest.fn();
  expect(Moxo.onTapWindowClose(callback)).toBe(mockSubscription);
  expect(mockNative.onTapWindowClose).toHaveBeenCalledWith(callback);
  expect(mockNative.enableTapWindowCloseEvent).toHaveBeenCalledTimes(1);
});
